/**
 * 生产环境日志工具
 * 在开发环境输出到控制台，生产环境可以集成日志服务
 */

type LogLevel = 'debug' | 'info' | 'warn' | 'error'

interface LoggerConfig {
  enabled: boolean
  level: LogLevel
  remoteLogging?: boolean
}

const LOG_LEVELS: Record<LogLevel, number> = {
  debug: 0,
  info: 1,
  warn: 2,
  error: 3
}

class Logger {
  private config: LoggerConfig

  constructor() {
    this.config = {
      // 生产环境可以通过环境变量控制
      enabled: import.meta.env.DEV || import.meta.env.PROD,
      level: import.meta.env.PROD ? 'error' : 'debug',
      remoteLogging: false
    }
  }

  private shouldLog(level: LogLevel): boolean {
    if (!this.config.enabled) return false
    return LOG_LEVELS[level] >= LOG_LEVELS[this.config.level]
  }

  private formatMessage(level: LogLevel, module: string, message: string): string {
    const timestamp = new Date().toISOString()
    return `[${timestamp}] [${level.toUpperCase()}] [${module}] ${message}`
  }

  debug(module: string, message: string, ...args: unknown[]): void {
    if (this.shouldLog('debug')) {
      console.debug(this.formatMessage('debug', module, message), ...args)
    }
  }

  info(module: string, message: string, ...args: unknown[]): void {
    if (this.shouldLog('info')) {
      console.info(this.formatMessage('info', module, message), ...args)
    }
  }

  warn(module: string, message: string, ...args: unknown[]): void {
    if (this.shouldLog('warn')) {
      console.warn(this.formatMessage('warn', module, message), ...args)
    }
  }

  error(module: string, message: string, error?: Error | unknown): void {
    if (this.shouldLog('error')) {
      const formattedMessage = this.formatMessage('error', module, message)
      if (error) {
        console.error(formattedMessage, error)
        // 生产环境可以在此处上报错误到监控系统
        if (this.config.remoteLogging && import.meta.env.PROD) {
          this.reportError(module, message, error)
        }
      } else {
        console.error(formattedMessage)
      }
    }
  }

  /**
   * 上报错误到远程日志服务（可扩展）
   */
  private reportError(module: string, message: string, error: unknown): void {
    // TODO: 集成 Sentry、LogRocket 等错误监控服务
    // 示例：
    // if (typeof window !== 'undefined' && (window as any).Sentry) {
    //   (window as any).Sentry.captureException(error, {
    //     tags: { module },
    //     extra: { message }
    //   })
    // }
  }

  setConfig(config: Partial<LoggerConfig>): void {
    this.config = { ...this.config, ...config }
  }
}

// 导出单例实例
export const logger = new Logger()

// 导出便捷方法
export const log = {
  debug: (module: string, message: string, ...args: unknown[]) => logger.debug(module, message, ...args),
  info: (module: string, message: string, ...args: unknown[]) => logger.info(module, message, ...args),
  warn: (module: string, message: string, ...args: unknown[]) => logger.warn(module, message, ...args),
  error: (module: string, message: string, error?: Error | unknown) => logger.error(module, message, error)
}

export default logger