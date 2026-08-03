<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>User Management Service</title>
    <link rel="icon" type="image/png" href="${url.resourcesPath}/images/user-management-logo.png" />
    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
</head>

<body>
    <div class="split-layout">
        <!-- SIDEBAR -->
        <aside class="sidebar">
            <div class="sidebar-wrapper">
                <div class="sidebar-brand-section">
                    <img src="${url.resourcesPath}/images/Shield-image.png" class="sidebar-shield">
                    <div class="sidebar-title-group">
                        <h1>USER</h1>
                        <h2>MANAGEMENT</h2>
                        <p>SERVICE</p>
                        <div class="sidebar-line"></div>
                        <small>LINK DEVELOPMENT</small>
                    </div>
                </div>

                <p class="sidebar-tagline">Secure identity management platform for Link Development employees.</p>

                <div class="sidebar-features">
                    <div class="feature-item">
                        <span class="f-icon">🛡️</span>
                        <div class="f-content"><strong>Enterprise Security</strong><p>Advanced protection for your data</p></div>
                    </div>
                    <div class="feature-item">
                        <span class="f-icon">👥</span>
                        <div class="f-content"><strong>Role Based Access</strong><p>Access control based on your role</p></div>
                    </div>
                    <div class="feature-item">
                        <span class="f-icon">🔄</span>
                        <div class="f-content"><strong>Single Sign On</strong><p>One account. All apps. Seamless access.</p></div>
                    </div>
                    <div class="feature-item">
                        <span class="f-icon">✅</span>
                        <div class="f-content"><strong>Identity Verification</strong><p>We verify you. You stay secure.</p></div>
                    </div>
                </div>

                <footer class="sidebar-footer">
                    © 2024 Link Development<br>All rights reserved.
                </footer>
            </div>
        </aside>

        <!-- MAIN AREA -->
        <main class="main-area">
            <header class="top-nav">
                <img src="${url.resourcesPath}/images/user-management-logo.png" class="nav-logo">
                <nav class="nav-links">
                    <a href="#">Help</a><a href="#">Privacy</a><a href="#">Support</a>
                </nav>
            </header>

            <div class="content-body">
                <#if displayMessage && message?has_content>
                    <div class="alert-banner alert-${message.type}">
                        ${message.summary}
                    </div>
                </#if>
                <#nested "header">
                <#nested "form">
            </div>
        </main>
    </div>
</body>
</html>
</#macro>