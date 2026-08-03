<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=messagesPerField.existsError('firstName','lastName','email','username','password','password-confirm'); section>
    <#if section = "header">
        <h1 class="design-title">Create Account</h1>
    <#elseif section = "form">
        <div class="setup-card" style="width: 550px; flex-direction: column; align-items: stretch; cursor: default;">
            <form action="${url.registrationAction}" method="post">
                <div class="pc-input-group">
                    <label for="firstName">First Name</label>
                    <input type="text" id="firstName" name="firstName" class="modern-control" value="${(register.formData.firstName!'')}" />
                </div>
                <div class="pc-input-group">
                    <label for="lastName">Last Name</label>
                    <input type="text" id="lastName" name="lastName" class="modern-control" value="${(register.formData.lastName!'')}" />
                </div>
                <div class="pc-input-group">
                    <label for="email">Email</label>
                    <input type="text" id="email" name="email" class="modern-control" value="${(register.formData.email!'')}" />
                </div>
                <button type="submit" class="btn-continue" style="width: 100%;">Register</button>
            </form>
        </div>
    </#if>
</@layout.registrationLayout>