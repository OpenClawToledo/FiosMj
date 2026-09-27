package com.fiosmj.app.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class PageController {

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
        return """
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
            """;
    }

    @GetMapping("/politica-de-privacidade")
    @ResponseBody
    public String privacidade() {
        return """
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
                <p><strong>Última atualização:</strong> Janeiro de 2026</p>
                <p>A Fios MJ (doravante "nós") está comprometida em proteger a privacidade dos nossos clientes. Esta política descreve como coletamos, usamos e protegemos suas informações.</p>

                <h2>1. Dados Coletados</h2>
                <p>Coletamos os seguintes dados pessoais:</p>
                <ul>
                  <li>Nome completo e endereço de entrega</li>
                  <li>Número de telefone (WhatsApp) e e-mail</li>
                  <li>Dados de pagamento (processados pelo Mercado Pago — nunca armazenamos dados de cartão)</li>
                  <li>Histórico de pedidos e preferências de compra</li>
                </ul>

                <h2>2. Como Usamos os Dados</h2>
                <ul>
                  <li>Processar e entregar seus pedidos</li>
                  <li>Comunicar atualizações de pedidos via WhatsApp</li>
                  <li>Melhorar nossos produtos e serviços</li>
                  <li>Cumprir obrigações legais</li>
                </ul>

                <h2>3. Mercado Pago</h2>
                <p>Utilizamos o Mercado Pago como plataforma de pagamento. Seus dados financeiros são tratados diretamente pelo Mercado Pago, seguindo sua própria <a href="https://www.mercadopago.com.br/privacidade" target="_blank">política de privacidade</a>.</p>

                <h2>4. Compartilhamento de Dados</h2>
                <p>Não vendemos nem compartilhamos seus dados pessoais com terceiros, exceto quando necessário para processar pagamentos ou cumprir obrigações legais.</p>

                <h2>5. Seus Direitos (LGPD)</h2>
                <p>Você tem direito a acessar, corrigir ou solicitar a exclusão dos seus dados. Entre em contato conosco pelo WhatsApp: <a href="https://wa.me/5533999892409">+55 33 99989-2409</a>.</p>

                <h2>6. Contato</h2>
                <p>Dúvidas? Fale conosco: <a href="https://wa.me/5533999892409">WhatsApp</a> ou <a href="https://instagram.com/fiosmjcroche">Instagram</a>.</p>

                <a href="/" class="back">← Voltar à Loja</a>
              </div>
            </body>
            </html>
            """;
    }

    @GetMapping("/termos-e-condicoes")
    @ResponseBody
    public String termos() {
        return """
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
                <p><strong>Última atualização:</strong> Janeiro de 2026</p>
                <p>Ao realizar uma compra na Fios MJ, você concorda com os termos abaixo.</p>

                <h2>1. Sobre os Produtos</h2>
                <ul>
                  <li>Todos os produtos são feitos artesanalmente sob encomenda</li>
                  <li>Pequenas variações na cor e tamanho são normais e fazem parte do charme artesanal</li>
                  <li>As fotos são representativas; o produto final pode ter pequenas diferenças</li>
                </ul>

                <h2>2. Prazo de Produção e Entrega</h2>
                <ul>
                  <li>Prazo de produção: 7 a 15 dias úteis após confirmação do pagamento</li>
                  <li>Prazo de entrega via Correios: 5 a 20 dias úteis conforme a região</li>
                  <li>Você receberá o código de rastreio via WhatsApp</li>
                </ul>

                <h2>3. Pagamento</h2>
                <ul>
                  <li>Aceitamos PIX, cartão de crédito/débito e boleto via Mercado Pago</li>
                  <li>A produção inicia somente após a confirmação do pagamento</li>
                </ul>

                <h2>4. Trocas e Devoluções</h2>
                <ul>
                  <li>Aceitamos trocas em caso de defeito de fabricação</li>
                  <li>Não aceitamos devoluções por arrependimento em produtos personalizados</li>
                  <li>Para solicitar troca, entre em contato em até 7 dias após o recebimento</li>
                </ul>

                <h2>5. Contato</h2>
                <p>Dúvidas? Fale conosco pelo <a href="https://wa.me/5533999892409">WhatsApp: +55 33 99989-2409</a></p>

                <a href="/" class="back">← Voltar à Loja</a>
              </div>
            </body>
            </html>
            """;
    }

    @GetMapping("/contacto")
    @ResponseBody
    public String contacto() {
        return """
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
            """;
    }

    @GetMapping("/pendente")
    @ResponseBody
    public String pendente() {
        return """
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
            """;
    }
}
