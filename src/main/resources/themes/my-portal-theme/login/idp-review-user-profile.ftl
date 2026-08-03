<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=messagesPerField.existsError('username','email','firstName','lastName'); section>
    <#if section = "header">
        <h1 class="design-title">Review Profile</h1>
    <#elseif section = "form">
        <p class="design-subtitle">Please confirm your information to complete your account setup.</p>

        <div class="setup-card" style="width: 600px; flex-direction: column; align-items: stretch; cursor: default; padding: 40px;">
            <form action="${url.loginAction}" method="post">

                <h3 style="color: #1e293b; margin-bottom: 15px; font-size: 18px; border-bottom: 1px solid #f1f5f9; padding-bottom: 10px;">Account Information</h3>

                <div class="pc-input-group">
                    <label for="email">Email Address</label>
                    <input type="text" id="email" name="email" value="${(user.email!'')}" class="modern-control" readonly style="background: #f1f5f9; cursor: not-allowed;" />
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
                    <div class="pc-input-group">
                        <label for="firstName">First Name</label>
                        <input type="text" id="firstName" name="firstName" value="${(user.firstName!'')}" class="modern-control" />
                    </div>
                    <div class="pc-input-group">
                        <label for="lastName">Last Name</label>
                        <input type="text" id="lastName" name="lastName" value="${(user.lastName!'')}" class="modern-control" />
                    </div>
                </div>

                <#-- DYNAMIC ATTRIBUTES WITH GROUP HEADERS -->
                <#if profile?? && profile.attributes??>
                    <#assign currentGroup = "">
                    <#list profile.attributes as attribute>
                        <#-- Skip standard fields -->
                        <#if attribute.name != "email" && attribute.name != "firstName" && attribute.name != "lastName" && attribute.name != "username">

                            <#-- Logic to show Group Header (e.g. DEVELOPER Details) -->
                            <#if attribute.group?? && attribute.group.name != currentGroup>
                                <#assign currentGroup = attribute.group.name>
                                <h3 style="color: #1e293b; margin: 25px 0 15px; font-size: 18px; border-bottom: 1px solid #f1f5f9; padding-bottom: 10px;">
                                    ${attribute.group.displayHeader!currentGroup}
                                </h3>
                            </#if>

                            <div class="pc-input-group">
                                <label for="${attribute.name}">${attribute.displayName!attribute.name}</label>
                                <input type="text"
                                       id="${attribute.name}"
                                       name="${attribute.name}"
                                       value="${(attribute.value!'')}"
                                       class="modern-control"
                                       placeholder="Enter ${attribute.displayName!attribute.name}" />
                            </div>
                        </#if>
                    </#list>
                </#if>

                <button type="submit" class="btn-continue" style="width: 100%; margin-top: 20px; height: 54px; font-weight: 700;">
                    Confirm and Finish &rarr;
                </button>
            </form>
        </div>
    </#if>
</@layout.registrationLayout>