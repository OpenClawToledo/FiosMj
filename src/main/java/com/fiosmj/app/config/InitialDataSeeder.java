package com.fiosmj.app.config;

import com.fiosmj.app.controller.ProductAdminController;
import com.fiosmj.app.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Servidor novo com banco vazio: carrega os produtos iniciais para a loja não abrir vazia.
 * Se já existir qualquer produto, não faz nada.
 */
@Component
public class InitialDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialDataSeeder.class);

    private final ProductRepository products;
    private final ProductAdminController productAdmin;

    public InitialDataSeeder(ProductRepository products, ProductAdminController productAdmin) {
        this.products = products;
        this.productAdmin = productAdmin;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (products.count() > 0) return;
        var seeds = productAdmin.buildSeeds();
        products.saveAll(seeds);
        log.info("Banco vazio: {} produtos iniciais carregados", seeds.size());
    }
}
