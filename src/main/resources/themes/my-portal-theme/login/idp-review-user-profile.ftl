<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=true; section>
   <#if section = "header">
        <h1 class="design-title">Complete Your Profile</h1>
    <#elseif section = "form">
        <div class="setup-card" style="width: 600px; flex-direction: column; align-items: stretch; cursor: default; padding: 40px;">
            <form action="${url.loginAction}" method="post" id="dynamic-profile-form">

                <h3 style="color: #1e293b; border-bottom: 1px solid #f1f5f9; padding-bottom: 10px;">Account Information</h3>

                <#-- 1. IDENTITY FIELDS -->
                <div class="pc-input-group">
                    <label for="username">Username</label>
                    <input type="text" id="username" name="username" value="${(user.username!'')}" class="modern-control" />
                </div>

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

                <#-- 2. DYNAMIC USER TYPE DROPDOWN -->
                <div class="pc-input-group">
                    <label>Account Type</label>
                    <select id="user_type_selector" name="user_type" class="modern-control" onchange="syncUI()">
                        <option value="">-- Select Your Role --</option>


                        <#assign roleListString = (realm.attributes['user_type_list']!"")>

                        <#if roleListString?has_content>
                            <#list roleListString?split(",") as type>
                                <#assign isSelected = false>
                                <#if profile?? && profile.attributes??>
                                    <#list profile.attributes as attr>
                                        <#if attr.name == "user_type" && ((attr.values![])?first!"")?upper_case == type?upper_case>
                                            <#assign isSelected = true>
                                        </#if>
                                    </#list>
                                </#if>
                                <option value="${type}" ${isSelected?then('selected','')}>${type?capitalize}</option>
                            </#list>
                        </#if>
                    </select>
                </div>

                <#-- 3. DYNAMIC GROUP RENDERING -->
                <#if profile?? && profile.attributes??>
                <#-- First, identify which groups actually exist on this page -->
                    <#assign foundGroups = []>
                    <#list profile.attributes as attr>
                        <#if attr.group?? && attr.group.name?contains("-group") && !foundGroups?seq_contains(attr.group.name)>
                            <#assign foundGroups = foundGroups + [attr.group.name]>
                        </#if>
                    </#list>

                <#-- Render a section for every group found -->
                    <#list foundGroups as gName>
                        <div id="group-section-${gName?lower_case}" class="role-specific-section" style="display: none; border-top: 1px dashed #cbd5e1; margin-top: 20px; padding-top: 10px;">
                            <h3 style="color: #1e293b; margin-bottom: 15px;">${gName?replace("-group", "")?upper_case} Details</h3>

                            <#list profile.attributes as attr>
                                <#if attr.group?? && attr.group.name == gName>
                                    <div class="pc-input-group">
                                        <label>${attr.displayName!attr.name}</label>
                                        <input type="text" name="${attr.name}" value="${(attr.values?first!'')}" class="modern-control" />
                                    </div>
                                </#if>
                            </#list>
                        </div>
                    </#list>
                </#if>

                <button type="submit" class="btn-continue" style="width: 100%; margin-top: 30px;">Complete Registration &rarr;</button>
            </form>
        </div>

        <script>
            function syncUI() {
                const selector = document.getElementById('user_type_selector');
                if(!selector) return;

                const selectedValue = selector.value.toLowerCase();

                document.querySelectorAll('.role-specific-section').forEach(section => {
                    section.style.display = 'none';
                    section.querySelectorAll('input').forEach(input => {
                        input.disabled = true;
                    });
                });

                if (selectedValue) {
                    const targetId = 'group-section-' + selectedValue + '-group';
                    const targetSection = document.getElementById(targetId);
                    if (targetSection) {
                        targetSection.style.display = 'block';
                        targetSection.querySelectorAll('input').forEach(input => {
                            input.disabled = false;
                        });
                    }
                }
            }
            window.onload = syncUI;
        </script>
    </#if>
</@layout.registrationLayout>