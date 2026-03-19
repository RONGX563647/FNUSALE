package com.fnusale.common.dto.amap;

import lombok.Data;

/**
 * 高德地图逆地理编码结果
 */
@Data
public class AmapGeocodeResult {

    /**
     * 状态码
     */
    private String status;

    /**
     * 状态信息
     */
    private String info;

    /**
     * 地址信息
     */
    private Regeocode regeocode;

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return "1".equals(status);
    }

    /**
     * 获取格式化地址
     */
    public String getFormattedAddress() {
        return regeocode != null ? regeocode.getFormattedAddress() : null;
    }

    /**
     * 获取省份
     */
    public String getProvince() {
        return regeocode != null && regeocode.getAddressComponent() != null 
            ? regeocode.getAddressComponent().getProvince() : null;
    }

    /**
     * 获取城市
     */
    public String getCity() {
        return regeocode != null && regeocode.getAddressComponent() != null 
            ? regeocode.getAddressComponent().getCity() : null;
    }

    /**
     * 获取区县
     */
    public String getDistrict() {
        return regeocode != null && regeocode.getAddressComponent() != null 
            ? regeocode.getAddressComponent().getDistrict() : null;
    }

    /**
     * 逆地理编码响应
     */
    @Data
    public static class Regeocode {
        private String formattedAddress;
        private AddressComponent addressComponent;
    }

    /**
     * 地址组件
     */
    @Data
    public static class AddressComponent {
        private String province;
        private String city;
        private String district;
        private String township;
        private String street;
        private String streetNumber;
    }
}
