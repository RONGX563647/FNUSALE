package com.fnusale.common.dto.amap;

import lombok.Data;

/**
 * 高德地图IP定位结果
 */
@Data
public class AmapLocationResult {

    /**
     * 状态码
     * 0：失败
     * 1：成功
     */
    private String status;

    /**
     * 状态信息
     */
    private String info;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 是否定位成功
     */
    public boolean isSuccess() {
        return "1".equals(status);
    }
}
