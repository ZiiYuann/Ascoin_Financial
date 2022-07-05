package com.tianli.management.tutorial.dto;

import com.tianli.management.tutorial.mapper.TutorialStatus;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author chensong
 * @date 2021-02-24 14:51
 * @since 1.0.0
 */
@Data
public class CreateTutorialDTO {
    /**
     * 标题
     */
    @NotBlank(message = "请输入标题<>Title is required<>โปรดป้อนชื่อ")
    private String title;

    /**
     * 正文
     */
    @NotBlank(message = "请输入正文<>Text is required<>กรุณากรอกข้อความ")
    private String text;

    /**
     * 标题 en
     */
    @NotBlank(message = "请输入英文标题<>En_title is required<>โปรดป้อนชื่อภาษาอังกฤษ")
    private String en_title;

    /**
     * 正文 en
     */
    @NotBlank(message = "请输入英文正文<>En_text is required<>กรุณากรอกข้อความภาษาอังกฤษ")
    private String en_text;

    /**
     * 标题 th_
     */
    @NotBlank(message = "请输入泰文标题<>Th_title is required<>กรุณาใส่ชื่อเรื่องภาษาไทย")
    private String th_title;

    /**
     * 正文 th_
     */
    @NotBlank(message = "请输入泰文正文<>Th_text is required<>กรุณากรอกข้อความเป็นภาษาไทย")
    private String th_text;

    /**
     * 状态（是否上线）
     */
    @NotNull(message = "请选择状态<>Status is required<>กรุณาเลือกสถานะ")
    private TutorialStatus status;
}
