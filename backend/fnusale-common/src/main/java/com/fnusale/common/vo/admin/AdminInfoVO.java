package com.fnusale.common.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员信息")
public class AdminInfoVO implements Serializable {

    @Schema(description = "管理员ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "角色（SUPER_ADMIN/OPERATOR/SERVICE）")
    private String role;

    @Schema(description = "权限列表")
    private List<String> permissions;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
