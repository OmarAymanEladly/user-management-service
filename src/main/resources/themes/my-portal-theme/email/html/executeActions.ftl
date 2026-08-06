<#outputformat "plainText">
<#-- This logic converts technical action names into readable text -->
    <#assign requiredActionsText><#if requiredActions??><#list requiredActions><#items as reqActionItem>${msg("requiredAction.${reqActionItem}")}<#sep>, </#sep></#items></#list></#if></#assign>
</#outputformat>

<#import "template.ftl" as layout>
<@layout.emailLayout>
    <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #333; line-height: 1.6;">
        <div style="background-color: #0046ad; padding: 20px; text-align: center; border-radius: 5px 5px 0 0;">
            <h1 style="color: #ffffff; margin: 0; font-size: 24px;">Welcome to the Portal</h1>
        </div>

        <div style="padding: 30px; border: 1px solid #e0e0e0; border-top: none; border-radius: 0 0 5px 5px;">
            <p style="font-size: 16px;">Hello,</p>

            <p style="font-size: 15px;">
                Your account has been successfully created. To finalize your registration and secure your account, the administrator requires you to perform the following action(s):
            </p>

            <div style="background-color: #f9f9f9; padding: 15px; border-left: 4px solid #0046ad; margin: 20px 0;">
                <strong style="color: #0046ad;">${requiredActionsText}</strong>
            </div>

            <p style="text-align: center; margin: 35px 0;">
                <a href="${link}" style="background-color: #0046ad; color: #ffffff; padding: 12px 25px; text-decoration: none; border-radius: 4px; font-weight: bold; display: inline-block;">
                    Complete Account Setup
                </a>
            </p>

            <p style="font-size: 13px; color: #666;">
                <strong>Note:</strong> This setup link is valid for <strong>${linkExpiration} minutes</strong>. If you did not request this, please ignore this email.
            </p>

            <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">

            <p style="font-size: 12px; color: #999; text-align: center;">
                &copy; 2026 User Management Service. All rights reserved.
            </p>
        </div>
    </div>
</@layout.emailLayout>