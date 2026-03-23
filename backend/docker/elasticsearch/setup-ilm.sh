#!/bin/bash

# ES索引生命周期管理(ILM)配置脚本
# 用于自动管理日志索引的生命周期

ES_HOST="${ES_HOST:-localhost}"
ES_PORT="${ES_PORT:-9200}"
ES_URL="http://${ES_HOST}:${ES_PORT}"

echo "配置ES索引生命周期管理策略..."

# 创建ILM策略
curl -X PUT "${ES_URL}/_ilm/policy/fnusale-logs-policy" -H 'Content-Type: application/json' -d'
{
  "policy": {
    "phases": {
      "hot": {
        "min_age": "0ms",
        "actions": {
          "rollover": {
            "max_size": "50gb",
            "max_age": "7d",
            "max_docs": 100000000
          },
          "set_priority": {
            "priority": 100
          }
        }
      },
      "warm": {
        "min_age": "7d",
        "actions": {
          "shrink": {
            "number_of_shards": 1
          },
          "forcemerge": {
            "max_num_segments": 1
          },
          "allocate": {
            "number_of_replicas": 0
          },
          "set_priority": {
            "priority": 50
          }
        }
      },
      "cold": {
        "min_age": "30d",
        "actions": {
          "freeze": {},
          "set_priority": {
            "priority": 0
          }
        }
      },
      "delete": {
        "min_age": "90d",
        "actions": {
          "delete": {}
        }
      }
    }
  }
}
'

echo ""
echo "创建索引模板..."

# 创建索引模板
curl -X PUT "${ES_URL}/_index_template/fnusale-logs-template" -H 'Content-Type: application/json' -d'
{
  "index_patterns": ["fnusale-logs-*"],
  "template": {
    "settings": {
      "number_of_shards": 3,
      "number_of_replicas": 1,
      "index.lifecycle.name": "fnusale-logs-policy",
      "index.lifecycle.rollover_alias": "fnusale-logs"
    },
    "mappings": {
      "properties": {
        "@timestamp": { "type": "date" },
        "trace_id": { "type": "keyword" },
        "span_id": { "type": "keyword" },
        "service": { "type": "keyword" },
        "serviceName": { "type": "keyword" },
        "level": { "type": "keyword" },
        "log_level": { "type": "keyword" },
        "logType": { "type": "keyword" },
        "log_type": { "type": "keyword" },
        "userId": { "type": "long" },
        "userRole": { "type": "keyword" },
        "clientIp": { "type": "ip" },
        "requestUri": { "type": "keyword" },
        "requestMethod": { "type": "keyword" },
        "duration": { "type": "long" },
        "httpStatus": { "type": "integer" },
        "message": { "type": "text" },
        "exception": { "type": "text" },
        "stackTrace": { "type": "text", "index": false },
        "requestParams": { "type": "text", "index": false },
        "responseBody": { "type": "text", "index": false }
      }
    }
  }
}
'

echo ""
echo "创建初始索引和别名..."

# 创建初始索引（如果不存在）
curl -X PUT "${ES_URL}/fnusale-logs-000001" -H 'Content-Type: application/json' -d'
{
  "aliases": {
    "fnusale-logs": {
      "is_write_index": true
    }
  }
}
'

echo ""
echo "ILM配置完成!"
