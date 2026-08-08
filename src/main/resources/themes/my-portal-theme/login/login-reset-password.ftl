<#import "template.ftl" as layout>

<@layout.registrationLayout
displayMessage=!messagesPerField.existsError('username')
displayInfo=false;
section
>

    <#if section = "header">

    <#elseif section = "form">

        <div class="forgot-password-container">

            <div class="forgot-password-card">

                <div class="forgot-password-icon">
                    <span>🔐</span>
                </div>

                <h1 class="forgot-password-title">
                    Forgot Your Password?
                </h1>

                <p class="forgot-password-subtitle">
                    No worries. Enter your username or email address and
                    we'll send you instructions to reset your password.
                </p>

                <#if message?has_content>
                    <div class="forgot-password-message">
                        ${kcSanitize(message.summary)?no_esc}
                    </div>
                </#if>

                <form
                        id="kc-reset-password-form"
                        action="${url.loginAction}"
                        method="post"
                >

                    <div class="forgot-input-group">

                        <label for="username">
                            Username or email
                        </label>

                        <input
                                id="username"
                                name="username"
                                type="text"
                                autofocus
                                autocomplete="username"
                                placeholder="Enter your username or email"
                        />

                    </div>

                    <button
                            class="forgot-submit-button"
                            type="submit"
                    >
                        Send Reset Link
                    </button>

                </form>

                <div class="forgot-back-link">
                    <a href="${url.loginUrl}">
                        ← Back to Login
                    </a>
                </div>

            </div>

        </div>

    </#if>

</@layout.registrationLayout>