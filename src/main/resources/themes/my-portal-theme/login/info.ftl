<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        <#-- Title is handled in form section for better alignment -->
    <#elseif section = "form">
        <h1 class="design-title">Complete Account Setup</h1>
        <p class="design-subtitle">Welcome! Before accessing your account, please complete the required security steps.</p>

        <#assign passwordRequired = false>
        <#assign emailRequired = false>
        <#if requiredActions??>
            <#list requiredActions as action>
                <#if action == "UPDATE_PASSWORD"><#assign passwordRequired = true></#if>
                <#if action == "VERIFY_EMAIL"><#assign emailRequired = true></#if>
            </#list>
        </#if>

        <#-- Update Password Card -->
        <div class="setup-card ${(!passwordRequired)?then('completed-card', '')}">
            <div class="sc-left">
                <#if passwordRequired>
                    <div class="icon-circle">🔒</div>
                    <div class="sc-text">
                        <strong>Update Password <span class="badge">Required</span></strong>
                        <p>Set a strong password to protect your account.</p>
                    </div>
                <#else>
                    <div class="icon-circle done">✔</div>
                    <div class="sc-text">
                        <strong>Update Password <span class="badge-done">Completed</span></strong>
                        <p>Your password has been successfully updated.</p>
                    </div>
                </#if>
            </div>
            <span class="sc-arrow">${passwordRequired?then('❯', '')}</span>
        </div>

        <#-- Verify Email Card -->
        <div class="setup-card ${(!emailRequired)?then('completed-card', '')}">
            <div class="sc-left">
                <#if emailRequired>
                    <div class="icon-circle">✉️</div>
                    <div class="sc-text">
                        <strong>Verify Email <span class="badge">Required</span></strong>
                        <p>Verify your email address to activate your account.</p>
                    </div>
                <#else>
                    <div class="icon-circle done">✔</div>
                    <div class="sc-text">
                        <strong>Verify Email <span class="badge-done">Completed</span></strong>
                        <p>Your email address is verified.</p>
                    </div>
                </#if>
            </div>
            <span class="sc-arrow">${emailRequired?then('❯', '')}</span>
        </div>

        <#if actionUri?has_content>
            <a href="${actionUri}" class="btn-continue">Continue &rarr;</a>
        </#if>
    </#if>
</@layout.registrationLayout>