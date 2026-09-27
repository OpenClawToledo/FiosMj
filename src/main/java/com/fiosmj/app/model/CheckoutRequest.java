package com.fiosmj.app.model;

import java.util.List;

public class CheckoutRequest {
    private List<CheckoutItem> items;
    private PayerInfo payer;
    private AddressInfo address;
    private String externalReference; // order ID para linkar com webhook

    public CheckoutRequest() {}

    public List<CheckoutItem> getItems() { return items; }
    public void setItems(List<CheckoutItem> items) { this.items = items; }

    public PayerInfo getPayer() { return payer; }
    public void setPayer(PayerInfo payer) { this.payer = payer; }

    public AddressInfo getAddress() { return address; }
    public void setAddress(AddressInfo address) { this.address = address; }
    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }

    // ── Inner classes ──────────────────────────────────────────────────────────

    public static class CheckoutItem {
        private Long productId;
        private String productName;
        private Double price;
        private Integer quantity;
        private String selectedSize;

        public CheckoutItem() {}

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public String getSelectedSize() { return selectedSize; }
        public void setSelectedSize(String selectedSize) { this.selectedSize = selectedSize; }
    }

    public static class PayerInfo {
        private String name;
        private String email;
        private String phone;
        private String cpf;

        public PayerInfo() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getCpf() { return cpf; }
        public void setCpf(String cpf) { this.cpf = cpf; }
    }

    public static class AddressInfo {
        private String cep;
        private String street;
        private String number;
        private String complement;
        private String neighborhood;
        private String city;
        private String state;

        public AddressInfo() {}

        public String getCep() { return cep; }
        public void setCep(String cep) { this.cep = cep; }

        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }

        public String getNumber() { return number; }
        public void setNumber(String number) { this.number = number; }

        public String getComplement() { return complement; }
        public void setComplement(String complement) { this.complement = complement; }

        public String getNeighborhood() { return neighborhood; }
        public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
    }
}
