package com.fiosmj.app.controller;

import com.fiosmj.app.model.SiteSetting;
import com.fiosmj.app.repository.SiteSettingRepository;
import com.fiosmj.app.security.AdminAuth;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Textos e contatos do site, editáveis pelo painel admin.
 * DEFAULTS define as chaves aceitas e o texto usado enquanto nada for alterado.
 */
@RestController
public class SiteSettingsController {

    static final Map<String, String> DEFAULTS = new LinkedHashMap<>();
    static {
        // Contatos
        DEFAULTS.put("whatsapp", "5533999892409");
        DEFAULTS.put("whatsappDisplay", "(33) 99989-2409");
        DEFAULTS.put("instagram", "fiosmjcroche");
        DEFAULTS.put("facebook", "https://facebook.com/fiosmjcroche");
        DEFAULTS.put("location", "Minas Gerais, Brasil");
        // Identificação da loja (obrigatória em loja online — Decreto 7.962/2013)
        DEFAULTS.put("lojaNome", "Fios MJ");
        DEFAULTS.put("lojaDocumento", "");
        DEFAULTS.put("lojaEndereco", "");
        DEFAULTS.put("lojaEmail", "");
        // Topo
        DEFAULTS.put("heroTitle", "Fios MJ");
        DEFAULTS.put("heroTagline", "💗 Crochê artesanal | Mãe & Filha 🧶");
        DEFAULTS.put("heroSubtitle", "Peças sob encomenda feitas com amor e dedicação 🎁");
        // Natal (aparece de 15/11 a 06/01)
        DEFAULTS.put("natalAviso", "Natal feito à mão · encomendas até 10/12");
        // Sobre
        DEFAULTS.put("aboutTitle", "💗 Sobre a Fios MJ");
        DEFAULTS.put("aboutSubtitle", "Conheça nossa história");
        DEFAULTS.put("aboutText1", "Somos **mãe e filha** unidas pela paixão pelo crochê! 🧶 Cada peça que criamos carrega carinho, dedicação e muitas horas de trabalho manual.");
        DEFAULTS.put("aboutText2", "Trabalhamos com encomendas personalizadas — você escolhe as cores, o tamanho e o modelo. Do amigurumi fofo à roupa estilosa, a gente faz com amor!");
        DEFAULTS.put("aboutHighlight", "✨ Crochê artesanal feito com o coração");
        DEFAULTS.put("stat1Number", "100+");
        DEFAULTS.put("stat1Label", "Peças entregues");
        DEFAULTS.put("stat2Number", "💯");
        DEFAULTS.put("stat2Label", "Feito à mão");
        DEFAULTS.put("stat3Number", "🇧🇷");
        DEFAULTS.put("stat3Label", "Envio nacional");
        // Como encomendar
        DEFAULTS.put("howSubtitle", "Em apenas 3 passos você tem sua peça artesanal exclusiva!");
        DEFAULTS.put("step1Title", "Escolha sua peça");
        DEFAULTS.put("step1Desc", "Navegue pelo catálogo e escolha o produto que você quer. Pode ser do catálogo ou uma peça personalizada!");
        DEFAULTS.put("step2Title", "Fale pelo WhatsApp");
        DEFAULTS.put("step2Desc", "Clique em \"Pedir\" e você será direcionada ao nosso WhatsApp com a mensagem já pronta. É fácil!");
        DEFAULTS.put("step3Title", "Receba com amor");
        DEFAULTS.put("step3Desc", "Combinamos prazo e pagamento, e sua peça é feita com todo carinho. Entregamos em todo o Brasil!");
        // Produtos
        DEFAULTS.put("customOrderText", "Não encontrou o que procura? Fazemos peças personalizadas! 💬");
        // Rodapé
        DEFAULTS.put("footerBio", "💗 Crochê artesanal | Mãe & Filha\n🧶 Peças sob encomenda 🎁");
        DEFAULTS.put("footerCopyright", "© 2026 Fios MJ · Crochê Artesanal · Feito com 💗");
    }

    private static final int MAX_LENGTH = 2000;

    private final SiteSettingRepository repo;
    private final AdminAuth adminAuth;

    public SiteSettingsController(SiteSettingRepository repo, AdminAuth adminAuth) {
        this.repo = repo;
        this.adminAuth = adminAuth;
    }

    /** Público: todos os textos (valor salvo ou padrão). */
    @GetMapping("/api/site")
    public Map<String, String> get() {
        Map<String, String> result = new LinkedHashMap<>(DEFAULTS);
        for (SiteSetting s : repo.findAll()) {
            if (DEFAULTS.containsKey(s.getKey()) && s.getValue() != null) result.put(s.getKey(), s.getValue());
        }
        return result;
    }

    /** Admin: salva os campos enviados. Valor vazio volta ao texto padrão. */
    @PutMapping("/api/admin/site")
    public ResponseEntity<?> update(@RequestHeader(value = "X-Admin-Secret", required = false) String secret,
                                    @RequestBody Map<String, Object> body) {
        if (!adminAuth.isValid(secret))
            return ResponseEntity.status(403).body(Map.of("error", "Não autorizado"));

        for (Map.Entry<String, Object> e : body.entrySet()) {
            String key = e.getKey();
            if (!DEFAULTS.containsKey(key)) continue;
            String value = e.getValue() == null ? "" : e.getValue().toString().trim();
            if (value.length() > MAX_LENGTH)
                return ResponseEntity.badRequest().body(Map.of("error", "Texto muito longo em " + key));
            if (key.equals("whatsapp")) value = value.replaceAll("\\D", "");
            if (key.equals("instagram")) value = value.replaceFirst("^@", "").replaceAll("\\s", "");

            if (value.isEmpty() || value.equals(DEFAULTS.get(key))) {
                if (repo.existsById(key)) repo.deleteById(key);
            } else {
                repo.save(new SiteSetting(key, value));
            }
        }
        return ResponseEntity.ok(get());
    }
}
