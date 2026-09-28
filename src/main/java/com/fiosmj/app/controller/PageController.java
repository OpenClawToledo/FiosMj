package com.fiosmj.app.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Map;
import java.nio.charset.StandardCharsets;

@Controller
public class PageController {

    private final SiteSettingsController siteSettings;

    public PageController(SiteSettingsController siteSettings) {
        this.siteSettings = siteSettings;
    }

    /** Aplica os contatos definidos no painel admin às páginas fixas. */
    private String fill(String html) {
        var s = siteSettings.get();
        String wa = s.getOrDefault("whatsapp", "").replaceAll("\\D", "");
        String ig = s.getOrDefault("instagram", "");
        return html
                .replace("__WA_URL__", "https://wa.me/" + wa)
                .replace("__WA_DISPLAY__", esc(s.getOrDefault("whatsappDisplay", "")))
                .replace("__IG_URL__", "https://instagram.com/" + esc(ig))
                .replace("__IG_HANDLE__", "@" + esc(ig))
                .replace("__LOJA_BLOCK__", lojaBlock(s))
                .replace("__LOJA_NOME__", esc(s.getOrDefault("lojaNome", "Fios MJ")))
                .replace("__CONTATO_PRIVACIDADE__", contatoPrivacidade(s, wa));
    }

    /** Identificação obrigatória da loja (Decreto 7.962/2013): nome, CNPJ/CPF, endereço e contato. */
    private static String lojaBlock(Map<String, String> s) {
        StringBuilder b = new StringBuilder("<ul>");
        b.append("<li><strong>Loja:</strong> ").append(esc(s.getOrDefault("lojaNome", "Fios MJ"))).append("</li>");
        String doc = s.getOrDefault("lojaDocumento", "");
        if (!doc.isBlank()) b.append("<li><strong>CNPJ/CPF:</strong> ").append(esc(doc)).append("</li>");
        String end = s.getOrDefault("lojaEndereco", "");
        if (!end.isBlank()) b.append("<li><strong>Endereço:</strong> ").append(esc(end)).append("</li>");
        String mail = s.getOrDefault("lojaEmail", "");
        if (!mail.isBlank()) b.append("<li><strong>E-mail:</strong> <a href=\"mailto:").append(esc(mail)).append("\">")
                .append(esc(mail)).append("</a></li>");
        String wa = s.getOrDefault("whatsappDisplay", "");
        if (!wa.isBlank()) b.append("<li><strong>WhatsApp:</strong> ").append(esc(wa)).append("</li>");
        return b.append("</ul>").toString();
    }

    private static String contatoPrivacidade(Map<String, String> s, String wa) {
        String mail = s.getOrDefault("lojaEmail", "");
        String link = "<a href=\"https://wa.me/" + wa + "\">WhatsApp " + esc(s.getOrDefault("whatsappDisplay", "")) + "</a>";
        if (!mail.isBlank()) link = "<a href=\"mailto:" + esc(mail) + "\">" + esc(mail) + "</a> ou " + link;
        return link;
    }

    private static String esc(String v) {
        return v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }


    @GetMapping("/admin")
    public ResponseEntity<byte[]> admin() throws IOException {
        var resource = new ClassPathResource("static/admin.html");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource.getInputStream().readAllBytes());
    }

    @GetMapping("/obrigado")
    @ResponseBody
    public String obrigado() {
        return fill("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>Pedido Confirmado – Fios MJ</title>
              <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body { font-family: 'Segoe UI', sans-serif; background: #fff5fb; min-height: 100vh;
                       display: flex; align-items: center; justify-content: center; padding: 20px; }
                .card { background: white; border-radius: 20px; padding: 48px 40px; max-width: 480px;
                        width: 100%; text-align: center; box-shadow: 0 8px 40px rgba(233,30,123,0.12); }
                .icon { font-size: 4rem; margin-bottom: 20px; }
                h1 { color: #e91e7b; font-size: 1.8rem; margin-bottom: 12px; }
                p { color: #666; font-size: 1rem; line-height: 1.6; margin-bottom: 24px; }
                a { display: inline-block; background: #e91e7b; color: white; padding: 14px 32px;
                    border-radius: 10px; text-decoration: none; font-weight: 700; font-size: 1rem; }
                a:hover { opacity: 0.9; }
              </style>
            </head>
            <body>
              <div class="card">
                <div class="icon">🧶✅</div>
                <h1>Pedido Confirmado!</h1>
                <p>Obrigada pela sua compra! 💕<br>
                   Em breve entraremos em contato via WhatsApp para confirmar os detalhes e o prazo de entrega.</p>
                <a href="/">Ver mais produtos</a>
              </div>
            </body>
            </html>
            """);
    }

    @GetMapping("/politica-de-privacidade")
    @ResponseBody
    public String privacidade() {
        return fill("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
              <meta charset="UTF-8"/><meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>Política de Privacidade – Fios MJ</title>
              <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body { font-family: 'Segoe UI', sans-serif; background: #fff5fb; padding: 20px; color: #333; }
                .wrap { max-width: 800px; margin: 40px auto; background: white; border-radius: 20px;
                        padding: 40px; box-shadow: 0 4px 20px rgba(233,30,123,0.1); }
                h1 { color: #e91e7b; margin-bottom: 24px; }
                h2 { color: #c2185b; margin: 24px 0 12px; }
                p, li { line-height: 1.7; margin-bottom: 10px; color: #555; }
                ul { padding-left: 20px; }
                a { color: #e91e7b; }
                .back { display: inline-block; margin-top: 24px; background: #e91e7b; color: white;
                        padding: 10px 24px; border-radius: 8px; text-decoration: none; font-weight: 600; }
              </style>
            </head>
            <body>
              <div class="wrap">
                <h1>🔒 Política de Privacidade</h1>
                <p><strong>Última atualização:</strong> setembro de 2026</p>
                <p>Esta política explica como a __LOJA_NOME__ trata os dados pessoais de quem visita e compra no site, conforme a Lei Geral de Proteção de Dados (Lei 13.709/2018 — LGPD).</p>

                <h2>1. Quem é a responsável pelos dados</h2>
                __LOJA_BLOCK__
                <p>Para qualquer assunto sobre seus dados, fale com a responsável pelo tratamento: __CONTATO_PRIVACIDADE__.</p>

                <h2>2. Dados que coletamos e para quê</h2>
                <ul>
                  <li><strong>Cadastro:</strong> nome, e-mail, telefone e senha (guardada de forma criptografada) — para você acompanhar seus pedidos. Base legal: execução de contrato.</li>
                  <li><strong>Pedido:</strong> nome, telefone, e-mail e endereço de entrega — para produzir, enviar e dar suporte ao pedido. Base legal: execução de contrato.</li>
                  <li><strong>Pagamento:</strong> feito no ambiente do Mercado Pago. Não recebemos nem guardamos dados de cartão. O CPF, quando informado, é repassado ao Mercado Pago e não fica salvo no nosso sistema.</li>
                  <li><strong>Newsletter:</strong> e-mail e nome (opcional) — só com o seu consentimento, que pode ser retirado a qualquer momento.</li>
                  <li><strong>Ranking de clientes:</strong> primeiro nome, inicial do sobrenome e número de pedidos — só para quem marcou essa opção no cadastro.</li>
                  <li><strong>Depoimentos:</strong> nome e texto que você enviar, publicados só após aprovação.</li>
                  <li><strong>Mensagens de contato:</strong> nome, contato e mensagem, para responder você.</li>
                </ul>

                <h2>3. Armazenamento no navegador</h2>
                <p>Não usamos cookies de publicidade. O site guarda no seu navegador (armazenamento local) apenas o necessário para funcionar: sua sessão de login, o carrinho, um identificador anônimo para contar visitas e a preferência do tema. Você pode apagar esses dados nas configurações do navegador.</p>

                <h2>4. Com quem compartilhamos</h2>
                <p>Não vendemos seus dados. Compartilhamos apenas o necessário com:</p>
                <ul>
                  <li><strong>Mercado Pago</strong> — processamento do pagamento (<a href="https://www.mercadopago.com.br/privacidade" target="_blank" rel="noopener">política do Mercado Pago</a>);</li>
                  <li><strong>Hostinger</strong> — servidor onde o site e o banco de dados ficam hospedados;</li>
                  <li><strong>Brevo</strong> — envio da newsletter, apenas para quem se inscreveu;</li>
                  <li><strong>Correios ou transportadora</strong> — entrega do pedido;</li>
                  <li>Autoridades, quando exigido por lei.</li>
                </ul>

                <h2>5. Por quanto tempo guardamos</h2>
                <ul>
                  <li>Dados de pedidos: pelo prazo exigido pelas leis fiscais e de defesa do consumidor (em geral, 5 anos).</li>
                  <li>Cadastro: enquanto a conta existir. Você pode pedir a exclusão a qualquer momento.</li>
                  <li>Newsletter: até você cancelar a inscrição.</li>
                </ul>

                <h2>6. Seus direitos</h2>
                <p>Você pode pedir, a qualquer momento: confirmação e acesso aos seus dados, correção, exclusão, portabilidade, informação sobre com quem compartilhamos e a retirada do consentimento. Respondemos em até 15 dias pelo contato do item 1. Você também pode reclamar à Autoridade Nacional de Proteção de Dados (ANPD).</p>

                <h2>7. Segurança</h2>
                <p>O site usa conexão criptografada (HTTPS), as senhas são guardadas com criptografia e o acesso ao painel é restrito. Nenhum sistema é totalmente imune a falhas; se ocorrer um incidente que traga risco a você, avisaremos você e a ANPD.</p>

                <a href="/" class="back">← Voltar à Loja</a>
              </div>
            </body>
            </html>
            """);
    }

    @GetMapping("/termos-e-condicoes")
    @ResponseBody
    public String termos() {
        return fill("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
              <meta charset="UTF-8"/><meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>Termos e Condições – Fios MJ</title>
              <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body { font-family: 'Segoe UI', sans-serif; background: #fff5fb; padding: 20px; color: #333; }
                .wrap { max-width: 800px; margin: 40px auto; background: white; border-radius: 20px;
                        padding: 40px; box-shadow: 0 4px 20px rgba(233,30,123,0.1); }
                h1 { color: #e91e7b; margin-bottom: 24px; }
                h2 { color: #c2185b; margin: 24px 0 12px; }
                p, li { line-height: 1.7; margin-bottom: 10px; color: #555; }
                ul { padding-left: 20px; }
                a { color: #e91e7b; }
                .back { display: inline-block; margin-top: 24px; background: #e91e7b; color: white;
                        padding: 10px 24px; border-radius: 8px; text-decoration: none; font-weight: 600; }
              </style>
            </head>
            <body>
              <div class="wrap">
                <h1>📋 Termos e Condições</h1>
                <p><strong>Última atualização:</strong> setembro de 2026</p>
                <p>Ao comprar na __LOJA_NOME__, você concorda com os termos abaixo, que seguem o Código de Defesa do Consumidor (Lei 8.078/1990) e o Decreto 7.962/2013.</p>

                <h2>1. Quem somos</h2>
                __LOJA_BLOCK__

                <h2>2. Sobre os produtos</h2>
                <ul>
                  <li>As peças são feitas à mão. Cada produto informa se é de <strong>pronta entrega</strong>, <strong>sob encomenda</strong> ou <strong>sob consulta</strong>.</li>
                  <li><strong>Peças personalizadas</strong> são as feitas com cor, tamanho, nome ou modelo escolhidos por você.</li>
                  <li>Pequenas variações de cor e medida são próprias do trabalho artesanal. As fotos mostram o modelo; variações grandes serão informadas antes.</li>
                </ul>

                <h2>3. Preço, pagamento e frete</h2>
                <ul>
                  <li>O preço válido é o mostrado na tela de pagamento do Mercado Pago no momento da compra.</li>
                  <li>Aceitamos PIX, cartão e boleto pelo Mercado Pago. A produção começa após a confirmação do pagamento.</li>
                  <li>O frete é informado pelo WhatsApp após o pedido, antes do envio.</li>
                </ul>

                <h2>4. Prazos</h2>
                <ul>
                  <li>Produção: o prazo aparece em cada produto. Quando não aparecer, é de 7 a 15 dias úteis após a confirmação do pagamento.</li>
                  <li>Entrega: de acordo com o prazo dos Correios ou da transportadora para a sua região. Enviamos o código de rastreio pelo WhatsApp.</li>
                </ul>

                <h2>5. Direito de arrependimento (7 dias)</h2>
                <ul>
                  <li>Em compras pela internet, você pode desistir em até <strong>7 dias corridos a partir do recebimento</strong> (art. 49 do CDC), sem precisar explicar o motivo.</li>
                  <li>Nesse caso devolvemos o valor pago, incluindo o frete de envio, pelo mesmo meio de pagamento. A peça deve voltar sem sinais de uso.</li>
                  <li>Peças <strong>personalizadas</strong> são produzidas exclusivamente para você e não podem ser revendidas. Para elas, combinamos os detalhes e aprovamos o modelo com você antes da produção.</li>
                  <li>Para desistir, fale conosco pelo contato do item 1 dentro do prazo.</li>
                </ul>

                <h2>6. Garantia e defeitos</h2>
                <ul>
                  <li>Se a peça chegar com defeito de fabricação ou diferente do combinado, você tem <strong>90 dias a partir do recebimento</strong> para reclamar (art. 26 do CDC).</li>
                  <li>Resolvemos em até 30 dias com conserto ou troca. Se não for possível, você escolhe entre outra peça, abatimento no preço ou devolução do valor.</li>
                  <li>Desgaste pelo uso normal e danos por lavagem ou uso inadequado não são defeitos.</li>
                </ul>

                <h2>7. Cancelamento pela loja</h2>
                <p>Se não conseguirmos produzir ou entregar o pedido, avisamos você e devolvemos o valor pago integralmente.</p>

                <h2>8. Atendimento</h2>
                <p>Dúvidas, trocas e reclamações: <a href="__WA_URL__">WhatsApp __WA_DISPLAY__</a>. Respondemos em até 5 dias úteis.</p>

                <a href="/" class="back">← Voltar à Loja</a>
              </div>
            </body>
            </html>
            """);
    }

    @GetMapping("/contacto")
    @ResponseBody
    public String contacto() {
        return fill("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
              <meta charset="UTF-8"/><meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>Contato – Fios MJ</title>
              <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body { font-family: 'Segoe UI', sans-serif; background: #fff5fb; padding: 20px; color: #333; }
                .wrap { max-width: 600px; margin: 40px auto; background: white; border-radius: 20px;
                        padding: 40px; box-shadow: 0 4px 20px rgba(233,30,123,0.1); }
                h1 { color: #e91e7b; margin-bottom: 8px; }
                .sub { color: #888; margin-bottom: 28px; }
                label { display: block; font-weight: 600; margin-bottom: 6px; color: #555; }
                input, textarea { width: 100%; padding: 12px 14px; border: 2px solid #fce4f0;
                        border-radius: 10px; font-size: 1rem; font-family: inherit; margin-bottom: 18px;
                        transition: border-color 0.2s; outline: none; }
                input:focus, textarea:focus { border-color: #e91e7b; }
                textarea { height: 140px; resize: vertical; }
                button { background: #e91e7b; color: white; border: none; padding: 14px 32px;
                        border-radius: 10px; font-size: 1rem; font-weight: 700; cursor: pointer;
                        font-family: inherit; width: 100%; transition: opacity 0.2s; }
                button:hover { opacity: 0.9; }
                .success { background: #e8f5e9; color: #2e7d32; padding: 14px; border-radius: 10px;
                           margin-bottom: 20px; display: none; }
                .back { display: inline-block; margin-top: 16px; color: #e91e7b; text-decoration: none;
                        font-weight: 600; }
              </style>
            </head>
            <body>
              <div class="wrap">
                <h1>💌 Fale Conosco</h1>
                <p class="sub">Ficou com dúvida? Envia sua mensagem que a Maju responde logo! 🧶</p>

                <div class="success" id="successMsg">✅ Mensagem enviada com sucesso! Responderemos em breve 💕</div>

                <form id="contactForm">
                  <label>Nome *</label>
                  <input type="text" name="name" placeholder="Seu nome" required />

                  <label>E-mail</label>
                  <input type="email" name="email" placeholder="seu@email.com" />

                  <label>Mensagem *</label>
                  <textarea name="message" placeholder="Escreva sua dúvida ou mensagem..." required></textarea>

                  <button type="submit">📨 Enviar Mensagem</button>
                </form>

                <br/>
                <a href="/" class="back">← Voltar à Loja</a>
              </div>

              <script>
                document.getElementById('contactForm').addEventListener('submit', async function(e) {
                  e.preventDefault();
                  const data = {
                    name: this.name.value,
                    email: this.email.value,
                    message: this.message.value
                  };
                  try {
                    const r = await fetch('/api/contact', {
                      method: 'POST',
                      headers: { 'Content-Type': 'application/json' },
                      body: JSON.stringify(data)
                    });
                    if (r.ok) {
                      document.getElementById('successMsg').style.display = 'block';
                      this.reset();
                    }
                  } catch(err) { alert('Erro ao enviar. Tente novamente.'); }
                });
              </script>
            </body>
            </html>
            """);
    }

    @GetMapping("/pendente")
    @ResponseBody
    public String pendente() {
        return fill("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>Pagamento em Análise – Fios MJ</title>
              <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                body { font-family: 'Segoe UI', sans-serif; background: #fff5fb; min-height: 100vh;
                       display: flex; align-items: center; justify-content: center; padding: 20px; }
                .card { background: white; border-radius: 20px; padding: 48px 40px; max-width: 480px;
                        width: 100%; text-align: center; box-shadow: 0 8px 40px rgba(233,30,123,0.12); }
                .icon { font-size: 4rem; margin-bottom: 20px; }
                h1 { color: #f59e0b; font-size: 1.8rem; margin-bottom: 12px; }
                p { color: #666; font-size: 1rem; line-height: 1.6; margin-bottom: 24px; }
                a { display: inline-block; background: #e91e7b; color: white; padding: 14px 32px;
                    border-radius: 10px; text-decoration: none; font-weight: 700; font-size: 1rem; }
                a:hover { opacity: 0.9; }
              </style>
            </head>
            <body>
              <div class="card">
                <div class="icon">🧶⏳</div>
                <h1>Pagamento em Análise</h1>
                <p>Seu pagamento está sendo processado.<br>
                   Assim que for confirmado, entraremos em contato via WhatsApp! 💕</p>
                <a href="/">Voltar à loja</a>
              </div>
            </body>
            </html>
            """);
    }
}
