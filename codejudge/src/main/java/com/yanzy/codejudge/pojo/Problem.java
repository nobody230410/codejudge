package com.yanzy.codejudge.pojo;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@TableName("problem")
public class Problem {

    /** 与库中 difficulty 枚举值 easy / middle / hard 对应 */
    public enum diff {
        EASY("easy"),
        MIDDLE("middle"),
        HARD("hard");

        @EnumValue
        private final String dbValue;

        diff(String dbValue) {
            this.dbValue = dbValue;
        }
    }

    private Integer id;

    private String title;

    private String description;

    private diff difficulty;

    /** 判题时间上限（秒），对应 Envoy body.options.timeLimit */
    @TableField("time_limit")
    private Double timeLimit;

    /** 内存上限（KB），对应 Envoy body.options.memoryLimit */
    @TableField("memory_limit")
    private Integer memoryLimit;

    @TableField("created_by")
    private Integer creator;

    private LocalDateTime createdAt;

}
