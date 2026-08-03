<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
        <#-- Title is handled inside the card below -->
    <#elseif section = "form">
        <div class="login-card">
            <#if message?has_content && message.type != 'warning'>
                <div class="alert-banner alert-${message.type}" style="background: #fee2e2; color: #991b1b; padding: 12px; border-radius: 8px; margin-bottom: 20px; font-size: 14px; border: 1px solid #fecaca;">
                    <span class="kc-feedback-text">${kcSanitize(message.summary)?no_esc}</span>
                </div>
            </#if>
            <h1 class="login-title">Sign In</h1>
            <p class="login-subtitle">Enter your credentials to access your account.</p>

            <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                <!-- USERNAME FIELD -->
                <div class="pc-input-group">
                    <label for="username">Username or email</label>
                    <input id="username" class="modern-control" name="username" value="${(login.username!'')}" type="text" autofocus autocomplete="off" placeholder="Enter your username" />
                </div>

                <!-- PASSWORD FIELD -->
                <div class="pc-input-group">
                    <label for="password">Password</label>
                    <input id="password" class="modern-control" name="password" type="password" autocomplete="off" placeholder="••••••••" />
                </div>

                <!-- HELPERS -->
                <div class="login-helper-row">
                    <#if realm.rememberMe && !login.rememberMe??>
                        <label class="remember-me-label">
                            <input type="checkbox" name="rememberMe"> Remember me
                        </label>
                    </#if>
                    <#if realm.resetPasswordAllowed>
                        <a href="${url.loginResetCredentialsUrl}" class="forgot-link">Forgot password?</a>
                    </#if>
                </div>

                <!-- SUBMIT BUTTON -->
                <button class="btn-enterprise-submit" name="login" id="kc-login" type="submit">
                    Sign In
                </button>
            </form>

            <#-- GOOGLE LOGIN -->
            <#if social.providers??>
                <div class="social-divider">
                    <span>Or sign in with</span>
                </div>
                <div class="social-btn-container">
                    <#list social.providers as p>
                        <a href="${p.loginUrl}" class="google-style-btn">
                             <img src="${url.resourcesPath}/images/google-logo.jpg" alt="Google">
                            Continue with ${p.displayName}
                        </a>
                    </#list>
                </div>
            </#if>

            <#-- SIGN UP LINK -->
            <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
                <div class="register-footer">
                    Don't have an account? <a href="${url.registrationUrl}">Sign Up</a>
                </div>
            </#if>
        </div>
    </#if>
</@layout.registrationLayout>