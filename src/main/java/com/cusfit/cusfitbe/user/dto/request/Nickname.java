package com.cusfit.cusfitbe.user.dto.request;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 닉네임 형식: 공백 없이 한글·영문·숫자 2~12자 (Figma 회원가입 안내 문구, BUSINESS_POLICY U-02). */
@NotBlank
@Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,12}$")
@ReportAsSingleViolation
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface Nickname {

	String message() default "닉네임은 공백 없이 한글, 영문, 숫자 2~12자여야 합니다";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
