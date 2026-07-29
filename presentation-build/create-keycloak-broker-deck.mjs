import fs from "node:fs/promises";
import path from "node:path";
import { Presentation, PresentationFile } from "@oai/artifact-tool";

const OUT_DIR = "C:/Users/HP/Desktop/user-management-service/presentation-build/output";
const FINAL = "C:/Users/HP/Desktop/user-management-service/keycloak-identity-brokering-project.pptx";

const C = {
  ink: "#111111",
  muted: "#555B63",
  light: "#F1F3F5",
  panel: "#E8EBEF",
  rule: "#B8BCC4",
  accent: "#3D8DFF",
  accent2: "#6DCBF4",
  ok: "#1F8A5B",
  warn: "#B36B00",
  white: "#FFFFFF",
};

function addText(slide, text, left, top, width, height, opts = {}) {
  const box = slide.shapes.add({
    geometry: "textbox",
    position: { left, top, width, height },
    fill: "none",
    line: { style: "solid", fill: "none", width: 0 },
  });
  box.text = text;
  box.text.style = {
    fontSize: opts.size ?? 22,
    bold: opts.bold ?? false,
    color: opts.color ?? C.ink,
    alignment: opts.align ?? "left",
    verticalAlignment: opts.valign ?? "top",
    fontFace: "Arial",
  };
  return box;
}

function addTitle(slide, title, subtitle, n) {
  addText(slide, title, 56, 42, 930, 64, { size: 37, bold: true });
  if (subtitle) addText(slide, subtitle, 58, 105, 820, 36, { size: 17, color: C.muted });
  slide.shapes.add({
    geometry: "rect",
    position: { left: 56, top: 152, width: 1168, height: 1.5 },
    fill: C.rule,
    line: { style: "solid", fill: C.rule, width: 0 },
  });
  addText(slide, String(n).padStart(2, "0"), 1166, 666, 58, 24, { size: 13, color: C.muted, align: "right" });
}

function addPanel(slide, left, top, width, height, fill = C.light) {
  return slide.shapes.add({
    geometry: "rect",
    position: { left, top, width, height },
    fill,
    line: { style: "solid", fill: C.rule, width: 1 },
  });
}

function bulletList(slide, items, left, top, width, opts = {}) {
  const lineH = opts.lineH ?? 35;
  items.forEach((item, i) => {
    slide.shapes.add({
      geometry: "ellipse",
      position: { left, top: top + i * lineH + 8, width: 8, height: 8 },
      fill: opts.dot ?? C.accent,
      line: { style: "solid", fill: opts.dot ?? C.accent, width: 0 },
    });
    addText(slide, item, left + 22, top + i * lineH, width - 22, lineH, { size: opts.size ?? 20, color: opts.color ?? C.ink });
  });
}

function step(slide, num, title, body, left, top, width) {
  addPanel(slide, left, top, width, 116, C.light);
  addText(slide, num, left + 18, top + 14, 46, 40, { size: 25, bold: true, color: C.accent });
  addText(slide, title, left + 74, top + 16, width - 94, 30, { size: 22, bold: true });
  addText(slide, body, left + 74, top + 52, width - 94, 48, { size: 16, color: C.muted });
}

function node(slide, label, detail, left, top, width, accent = C.accent) {
  addPanel(slide, left, top, width, 118, C.white);
  slide.shapes.add({
    geometry: "rect",
    position: { left, top, width: 8, height: 118 },
    fill: accent,
    line: { style: "solid", fill: accent, width: 0 },
  });
  addText(slide, label, left + 26, top + 20, width - 44, 30, { size: 23, bold: true });
  addText(slide, detail, left + 26, top + 58, width - 44, 44, { size: 15, color: C.muted });
}

function arrow(slide, x1, y1, x2, y2) {
  slide.shapes.add({
    geometry: "rightArrow",
    position: { left: x1, top: y1, width: x2 - x1, height: y2 - y1 },
    fill: C.panel,
    line: { style: "solid", fill: C.rule, width: 1 },
  });
}

function notes(slide, lines, sources = []) {
  const sourceBlock = sources.length
    ? ["", "[Sources]", ...sources.map((s) => `- ${s}`)]
    : ["", "[Sources]", "- Based on the local project configuration and Keycloak setup discussed in this workspace."];
  slide.speakerNotes.textFrame.setText([...lines, ...sourceBlock]);
  slide.speakerNotes.setVisible(true);
}

async function main() {
  await fs.mkdir(OUT_DIR, { recursive: true });
  await fs.writeFile(path.join(OUT_DIR, "source-notes.txt"), [
    "Deck sources:",
    "- user-management-service application.properties and SecurityConfig discussed in workspace.",
    "- task-management-api application.yaml and SecurityConfig inspected from local project.",
    "- Keycloak broker setup discussed in conversation: local realm task-management-demo, provider alias cloud-iam, Cloud-IAM realm user-management-dev.",
  ].join("\n"));

  const p = Presentation.create({ slideSize: { width: 1280, height: 720 } });

  let s = p.slides.add();
  s.background.fill = C.white;
  addText(s, "Keycloak Identity Brokering", 56, 88, 700, 80, { size: 54, bold: true });
  addText(s, "How the task-management API uses a local Keycloak broker with Cloud-IAM as the external identity provider", 58, 190, 760, 86, { size: 24, color: C.muted });
  addPanel(s, 820, 86, 350, 430, C.light);
  node(s, "Spring Boot API", "localhost:18080", 856, 132, 278, C.ink);
  node(s, "Identity Broker", "Local Keycloak on localhost:8081", 856, 282, 278, C.accent);
  node(s, "Identity Provider", "Cloud-IAM Keycloak realm user-management-dev", 856, 432, 278, C.accent2);
  addText(s, "Presentation goal: make the flow repeatable, testable, and easy to explain.", 58, 610, 820, 34, { size: 20 });
  notes(s, [
    "Open by saying this is not just a Keycloak feature demo. It explains which system issues tokens, which system authenticates external users, and how the Spring Boot app validates the result.",
    "The main distinction: Cloud-IAM authenticates, but the local Keycloak broker issues the API token."
  ]);

  s = p.slides.add(); s.background.fill = C.white;
  addTitle(s, "The system has three moving parts", "Each part has one clear security responsibility.", 2);
  node(s, "Spring Boot app", "Resource server. It does not log users in; it validates JWT access tokens.", 74, 212, 330, C.ink);
  node(s, "Identity broker", "Local Keycloak. It owns the realm trusted by the API and can redirect login to external providers.", 475, 212, 330, C.accent);
  node(s, "Identity provider", "Cloud-IAM Keycloak. It authenticates the external user and sends identity claims back to the broker.", 876, 212, 330, C.accent2);
  arrow(s, 410, 252, 462, 292);
  arrow(s, 811, 252, 863, 292);
  addText(s, "The API accepts tokens from the broker only.", 76, 514, 560, 34, { size: 26, bold: true });
  addText(s, "That keeps authorization centralized in the local realm even when the user originally signs in through Cloud-IAM.", 76, 560, 830, 52, { size: 20, color: C.muted });
  notes(s, [
    "Explain the vocabulary first: resource server, broker, provider.",
    "The broker is the middle Keycloak. It starts the login, delegates authentication, receives the callback, creates or links a local user, and issues the final local token."
  ]);

  s = p.slides.add(); s.background.fill = C.white;
  addTitle(s, "Environment setup uses localhost for the app and broker", "This is the concrete development topology we configured.", 3);
  addPanel(s, 64, 188, 530, 330, C.light);
  addText(s, "Local services", 92, 216, 250, 32, { size: 26, bold: true });
  bulletList(s, [
    "task-management-api: http://localhost:18080",
    "Local Keycloak broker: http://localhost:8081",
    "Local broker realm: task-management-demo",
    "Local broker client: task-management-client"
  ], 96, 274, 450, { size: 19, lineH: 44 });
  addPanel(s, 666, 188, 530, 330, C.light);
  addText(s, "External provider", 694, 216, 300, 32, { size: 26, bold: true });
  bulletList(s, [
    "Cloud-IAM base: https://lemur-2.cloud-iam.com/auth",
    "Provider realm: user-management-dev",
    "Broker callback alias: cloud-iam",
    "Callback URL ends with /broker/cloud-iam/endpoint"
  ], 698, 274, 446, { size: 19, lineH: 44, dot: C.accent2 });
  notes(s, [
    "This is the setup slide your team lead will care about. The app and the broker are local, while the external identity provider is Cloud-IAM.",
    "If the alias changes, the callback URL changes. That was one of the key troubleshooting points."
  ]);

  s = p.slides.add(); s.background.fill = C.white;
  addTitle(s, "Spring Boot integrates with the broker using OIDC", "The API validates the issuer and roles from local Keycloak tokens.", 4);
  addPanel(s, 70, 210, 540, 220, C.light);
  addText(s, "application.yaml", 100, 236, 260, 28, { size: 24, bold: true });
  addText(s, "spring.security.oauth2.resourceserver.jwt.issuer-uri:\nhttp://localhost:8081/realms/task-management-demo", 100, 292, 470, 82, { size: 20, color: C.ink });
  addPanel(s, 690, 210, 520, 220, C.light);
  addText(s, "SecurityConfig", 720, 236, 260, 28, { size: 24, bold: true });
  addText(s, "Every request is authenticated.\nJWT roles are read from realm_access.roles.\nSpring sees PORTAL_ADMIN as ROLE_PORTAL_ADMIN.", 720, 290, 430, 100, { size: 20, color: C.ink });
  addText(s, "Meaning: Cloud-IAM tokens are not accepted directly. The app trusts only tokens issued by the local broker realm.", 82, 510, 980, 60, { size: 26, bold: true });
  notes(s, [
    "The API is a resource server. It is not configured with Cloud-IAM as issuer. It is configured with the local Keycloak issuer.",
    "That is why the final JWT must have iss equal to http://localhost:8081/realms/task-management-demo."
  ], ["Local task-management-api application.yaml and SecurityConfig inspected from C:/Users/HP/Desktop/task-management-api."]);

  s = p.slides.add(); s.background.fill = C.white;
  addTitle(s, "The broker integrates with Cloud-IAM as an OIDC identity provider", "Local Keycloak uses a confidential client created in Cloud-IAM.", 5);
  step(s, "1", "Create client in Cloud-IAM", "Client ID local-keycloak-broker, client authentication ON, standard flow ON.", 70, 198, 540);
  step(s, "2", "Add redirect URI", "http://localhost:8081/realms/task-management-demo/broker/cloud-iam/endpoint", 70, 342, 540);
  step(s, "3", "Create identity provider locally", "Provider type OpenID Connect v1.0, alias cloud-iam, discovery endpoint from Cloud-IAM.", 670, 198, 540);
  step(s, "4", "Paste client secret", "Local Keycloak uses the Cloud-IAM client ID and secret to complete the OIDC code exchange.", 670, 342, 540);
  addText(s, "Discovery endpoint", 78, 542, 220, 26, { size: 21, bold: true });
  addText(s, "https://lemur-2.cloud-iam.com/auth/realms/user-management-dev/.well-known/openid-configuration", 78, 582, 1040, 32, { size: 20, color: C.accent });
  notes(s, [
    "Walk through this as a handshake. Cloud-IAM needs to know where to return the user. Local Keycloak needs to know Cloud-IAM's auth, token, userinfo, and JWKS endpoints.",
    "The discovery URL fills most of the endpoint fields automatically."
  ]);

  s = p.slides.add(); s.background.fill = C.white;
  addTitle(s, "Mappers decide what the broker imports or assigns", "Without mappers, the user may log in but miss useful local attributes or roles.", 6);
  const mapperRows = [
    ["Username Template Importer", "${CLAIM.preferred_username}", "Local username"],
    ["Attribute Importer", "email", "Local email attribute"],
    ["Attribute Importer", "given_name / family_name", "First and last name"],
    ["Hardcoded Role", "PORTAL_ADMIN", "Local authorization role"]
  ];
  addPanel(s, 70, 190, 1120, 340, C.light);
  addText(s, "Mapper type", 104, 224, 270, 26, { size: 21, bold: true });
  addText(s, "Input", 430, 224, 320, 26, { size: 21, bold: true });
  addText(s, "Result in local Keycloak", 800, 224, 300, 26, { size: 21, bold: true });
  mapperRows.forEach((r, i) => {
    const y = 278 + i * 58;
    slideRule(s, 100, y - 14, 1040);
    addText(s, r[0], 104, y, 270, 28, { size: 18, bold: i === 3 });
    addText(s, r[1], 430, y, 330, 28, { size: 18, color: i === 3 ? C.accent : C.ink });
    addText(s, r[2], 800, y, 320, 28, { size: 18, color: C.muted });
  });
  addText(s, "For production, replace hardcoded admin role with group or claim-based mapping.", 82, 580, 930, 34, { size: 22, bold: true, color: C.warn });
  notes(s, [
    "Explain that the hardcoded role is useful for proving the flow, but not a final production authorization model.",
    "A safer production setup maps a specific Cloud-IAM group or claim to a local role."
  ]);

  s = p.slides.add(); s.background.fill = C.white;
  addTitle(s, "Login has two redirects, but one final trusted token", "This is the manual flow to demonstrate in the browser.", 7);
  node(s, "1. Start at local Keycloak", "http://localhost:8081/realms/task-management-demo/account", 70, 212, 300, C.ink);
  node(s, "2. Choose Cloud-IAM", "The login page redirects to the external provider.", 394, 212, 260, C.accent);
  node(s, "3. Authenticate externally", "Cloud-IAM validates the username, password, MFA, or external session.", 678, 212, 260, C.accent2);
  node(s, "4. Return to local realm", "Local Keycloak links/imports the user and issues its own token.", 962, 212, 250, C.ok);
  arrow(s, 373, 250, 390, 292);
  arrow(s, 657, 250, 674, 292);
  arrow(s, 941, 250, 958, 292);
  addText(s, "The success condition is not just a Cloud-IAM login. It is landing back in the local realm.", 82, 520, 960, 62, { size: 28, bold: true });
  notes(s, [
    "This is the demo slide. Start from the local account URL. Do not start from Cloud-IAM directly, because then you are not testing the broker.",
    "After login, show the local Keycloak Users page and the imported user."
  ]);

  s = p.slides.add(); s.background.fill = C.white;
  addTitle(s, "Token testing proves which Keycloak the API trusts", "The issuer claim is the fastest way to explain success or failure.", 8);
  addPanel(s, 70, 190, 520, 300, C.light);
  addText(s, "Accepted by task-management-api", 104, 226, 390, 30, { size: 24, bold: true, color: C.ok });
  addText(s, "iss = http://localhost:8081/realms/task-management-demo", 104, 292, 430, 58, { size: 22 });
  addText(s, "This token comes from the local broker realm.", 104, 390, 390, 34, { size: 19, color: C.muted });
  addPanel(s, 690, 190, 520, 300, C.light);
  addText(s, "Rejected directly by the API", 724, 226, 390, 30, { size: 24, bold: true, color: C.warn });
  addText(s, "iss = https://lemur-2.cloud-iam.com/auth/realms/user-management-dev", 724, 292, 430, 82, { size: 22 });
  addText(s, "This token comes from the provider, not the broker.", 724, 410, 390, 34, { size: 19, color: C.muted });
  addText(s, "Rule of thumb: the Spring Boot issuer-uri must match the JWT iss claim exactly.", 82, 560, 1000, 34, { size: 25, bold: true });
  notes(s, [
    "In Postman or jwt.io, decode the token and point to the iss claim. This avoids long explanations.",
    "If the issuer does not match the Spring Boot issuer-uri, the resource server rejects the request."
  ]);

  s = p.slides.add(); s.background.fill = C.white;
  addTitle(s, "Manual Keycloak test checklist", "Use this as the live demo path after configuration.", 9);
  step(s, "A", "Open local account page", "http://localhost:8081/realms/task-management-demo/account", 70, 190, 540);
  step(s, "B", "Click Cloud-IAM on the login page", "This confirms the identity provider is enabled and visible.", 70, 334, 540);
  step(s, "C", "Login with Cloud-IAM user", "The browser should return to the local Keycloak account page.", 670, 190, 540);
  step(s, "D", "Verify imported user", "Local admin console -> task-management-demo -> Users -> Federated identity cloud-iam.", 670, 334, 540);
  addText(s, "If it does not return, check redirect URI, alias, client secret, and provider endpoint first.", 82, 565, 1000, 34, { size: 23, bold: true, color: C.warn });
  notes(s, [
    "This slide is your live demo checklist. The last verification is inside the local Keycloak admin console.",
    "When the user is imported, open the user and show Federated identity. Then show Role mapping if the hardcoded role mapper was used."
  ]);

  s = p.slides.add(); s.background.fill = C.white;
  addTitle(s, "The setup is successful when these four checks pass", "These are the acceptance criteria for the team.", 10);
  bulletList(s, [
    "Local Keycloak shows Cloud-IAM as a login option.",
    "Cloud-IAM login returns back to the local realm.",
    "The user appears in local Keycloak with federated identity cloud-iam.",
    "task-management-api accepts local Keycloak tokens and rejects direct Cloud-IAM tokens."
  ], 110, 210, 900, { size: 25, lineH: 60, dot: C.ok });
  addText(s, "Final message", 110, 520, 260, 30, { size: 24, bold: true });
  addText(s, "Cloud-IAM authenticates the user; local Keycloak brokers the login and issues the token the API trusts.", 110, 565, 900, 60, { size: 28, bold: true });
  notes(s, [
    "Close with the crisp summary: provider authenticates, broker issues, API validates.",
    "Mention the future production step: replace hardcoded role mapper with claim or group-based role mapping."
  ]);

  for (const [i, slide] of p.slides.items.entries()) {
    const png = await p.export({ slide, format: "png", scale: 1 });
    await fs.writeFile(path.join(OUT_DIR, `slide-${String(i + 1).padStart(2, "0")}.png`), new Uint8Array(await png.arrayBuffer()));
    const layout = await slide.export({ format: "layout" });
    await fs.writeFile(path.join(OUT_DIR, `slide-${String(i + 1).padStart(2, "0")}.layout.json`), await layout.text());
  }
  const montage = await p.export({ format: "webp", montage: true, scale: 1 });
  await fs.writeFile(path.join(OUT_DIR, "deck-montage.webp"), new Uint8Array(await montage.arrayBuffer()));
  const pptx = await PresentationFile.exportPptx(p);
  await pptx.save(FINAL);
}

function slideRule(slide, left, top, width) {
  slide.shapes.add({
    geometry: "rect",
    position: { left, top, width, height: 1 },
    fill: C.rule,
    line: { style: "solid", fill: C.rule, width: 0 },
  });
}

main().catch((err) => {
  console.error(err);
  process.exitCode = 1;
});
