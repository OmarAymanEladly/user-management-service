<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=true; section>
    <#if section = "header">
        <#-- Leave empty as we handle titles inside the card -->
    <#elseif section = "form">
        <div class="password-card-layout">
            <!-- LEFT SIDE: Fixed Scaling Illustration -->
            <div class="pc-side-illustration">
                <img src="${url.resourcesPath}/images/Shield-image.png">
            </div>

            <!-- RIGHT SIDE: Dynamic Form -->
            <div class="pc-side-form">
                <h1 class="pc-form-title">Update Password</h1>
                <p class="pc-form-subtitle">Protect your account by choosing a strong password.</p>
                <div class="pc-blue-alert">🛡️ For your security, choose a strong password that you haven't used before.</div>

                <form action="${url.loginAction}" method="post">
                    <!-- New Password Input -->
                    <div class="pc-input-group">
                        <label>New Password</label>
                        <input type="password" id="password-new" name="password-new" placeholder="••••••••" oninput="validatePassword()" autofocus autocomplete="new-password">
                    </div>

                    <!-- Dynamic Strength Meter -->
                    <div class="pc-strength-box">
                        <p>Password Strength: <span id="strength-text" style="font-weight: 700;">Empty</span></p>
                        <div class="pc-strength-bars">
                            <div id="bar-1" class="s-bar"></div>
                            <div id="bar-2" class="s-bar"></div>
                            <div id="bar-3" class="s-bar"></div>
                            <div id="bar-4" class="s-bar"></div>
                            <div id="bar-5" class="s-bar"></div>
                        </div>
                    </div>

                    <!-- Dynamic Checklist -->
                    <div class="pc-checklist">
                        <span id="req-length">✔ At least 8 characters</span>
                        <span id="req-number">✔ One number</span>
                        <span id="req-upper">✔ One uppercase letter</span>
                        <span id="req-special">✔ One special character</span>
                    </div>

                    <!-- Confirm Password -->
                    <div class="pc-input-group">
                        <label>Confirm Password</label>
                        <input type="password" name="password-confirm" placeholder="••••••••" autocomplete="new-password">
                    </div>

                    <button type="submit" class="btn-enterprise-submit">Update Password 🔒</button>
                </form>
            </div>
        </div>

        <!-- Logic Script placed at the end of the form section -->
        <script>
        function validatePassword() {
            const pass = document.getElementById('password-new').value;
            const strengthText = document.getElementById('strength-text');

            const checks = {
                length: pass.length >= 8,
                number: /[0-9]/.test(pass),
                upper: /[A-Z]/.test(pass),
                special: /[!@#$%^&*(),.?":{}|<>]/.test(pass)
            };

            // Toggle colors
            document.getElementById('req-length').className = checks.length ? 'act' : '';
            document.getElementById('req-number').className = checks.number ? 'act' : '';
            document.getElementById('req-upper').className = checks.upper ? 'act' : '';
            document.getElementById('req-special').className = checks.special ? 'act' : '';

            const score = Object.values(checks).filter(Boolean).length;

            // Update Bars
            for (let i = 1; i <= 5; i++) {
                const bar = document.getElementById('bar-' + i);
                if (pass.length === 0) {
                    bar.className = 's-bar';
                } else {
                    bar.className = (i <= score + 1) ? 's-bar act' : 's-bar';
                }
            }

            // Update Text colors and content
            if (pass.length === 0) {
                strengthText.innerText = "Empty";
                strengthText.style.color = "#94a3b8";
            } else if (score < 2) {
                strengthText.innerText = "Weak";
                strengthText.style.color = "#ef4444";
            } else if (score < 4) {
                strengthText.innerText = "Medium";
                strengthText.style.color = "#f59e0b";
            } else {
                strengthText.innerText = "Strong";
                strengthText.style.color = "#22c55e";
            }
        }
        </script>
    </#if>
</@layout.registrationLayout>