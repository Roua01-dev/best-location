package com.example.bestlocation.Model;

public class Position {
    private int id; private String pseudo,longitude,latitide ,numero;
    private String address;
    public Position(int id, String numero, String latitide, String longitude, String pseudo) {
        this.id = id;
        this.numero = numero;
        this.latitide = latitide;
        this.longitude = longitude;
        this.pseudo = pseudo;
    }
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitide() {
        return latitide;
    }

    public void setLatitide(String latitide) {
        this.latitide = latitide;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Position(){

    }

    @Override
    public String toString() {
        return "Position{" +
                "id=" + id +
                ", pseudo='" + pseudo + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitide='" + latitide + '\'' +
                ", numero='" + numero + '\'' +
                '}';
    }
}
