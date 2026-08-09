<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
    <#-- Title is handled inside the card below -->
    <#elseif section = "form">
        <div class="login-card">
            <#-- 1. ERROR BANNER (Added id="kc-error-message") -->
            <#if message?has_content && message.type != 'warning'>
                <div class="alert-banner alert-${message.type}" style="background: #fee2e2; color: #991b1b; padding: 12px; border-radius: 8px; margin-bottom: 20px; font-size: 14px; border: 1px solid #fecaca; text-align: center;">
                    <span id="kc-error-message" class="kc-feedback-text">${message.summary?no_esc}</span>
                </div>
            </#if>

            <h1 class="login-title">Sign In</h1>
            <p class="login-subtitle">Enter your credentials to access your account.</p>

            <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                <div class="pc-input-group">
                    <label for="username">Username or email</label>
                    <input id="username" class="modern-control" name="username" value="${(login.username!'')}" type="text" autofocus autocomplete="off" placeholder="Enter your username" />
                </div>

                <div class="pc-input-group">
                    <label for="password">Password</label>
                    <input id="password" class="modern-control" name="password" type="password" autocomplete="off" placeholder="••••••••" />
                </div>

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

                <button class="btn-enterprise-submit" name="login" id="kc-login" type="submit">Sign In</button>
            </form>

            <#if social.providers??>
                <div class="social-divider"><span>Or sign in with</span></div>
                <div class="social-btn-container">
                    <#list social.providers as p>
                        <a href="${p.loginUrl}" class="google-style-btn">
                            <#if p.alias == "google">
                                <img src="${url.resourcesPath}/images/google-logo.jpg" alt="Google">
                            <#else>
                                <img src="${url.resourcesPath}/images/oidc-logo.png" alt="OpenID Connect" class="oidc-logo">
                            </#if>
                            Continue with ${p.displayName}
                        </a>
                    </#list>
                </div>
            </#if>

            <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
                <div class="register-footer">
                    Don't have an account? <a href="${url.registrationUrl}">Sign Up</a>
                </div>
            </#if>
        </div>


        <script>
            document.addEventListener("DOMContentLoaded", function() {
                const errorElement = document.getElementById('kc-error-message');
                const usernameInput = document.getElementById('username');
                const MAX_ATTEMPTS = 3;


                if (!errorElement) {
                    Object.keys(localStorage).forEach(key => {
                        if (key.startsWith('kc_fails_') || key.startsWith('kc_locked_')) {
                            localStorage.removeItem(key);
                        }
                    });
                    return;
                }

                const currentUsername = usernameInput.value.trim().toLowerCase();

                if (currentUsername) {
                    const userKey = 'kc_fails_' + currentUsername;
                    const lockKey = 'kc_locked_' + currentUsername;
                    const msg = errorElement.innerText.toLowerCase();


                    if (msg.includes("locked") || msg.includes("disabled") || msg.includes("temporarily")) {
                        errorElement.innerText = "This account is currently locked. Please try again in 5 minutes.";
                        localStorage.setItem(lockKey, 'true');
                        localStorage.setItem(userKey, MAX_ATTEMPTS);
                        return;
                    }


                    let fails = parseInt(localStorage.getItem(userKey) || 0);
                    fails++;
                    localStorage.setItem(userKey, fails);

                    let remaining = MAX_ATTEMPTS - fails;

                    if (remaining > 0) {
                        errorElement.innerText = "Invalid credentials. " + remaining + " attempts remaining.";
                    } else {
                        errorElement.innerText = "Maximum attempts reached. This account is now locked.";
                        localStorage.setItem(lockKey, 'true');
                    }
                }
            });
        </script>
    </#if>
</@layout.registrationLayout>