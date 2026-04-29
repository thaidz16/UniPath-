package com.thai.unipath.model;

import java.time.LocalDate;

public class ThiSinh extends Nguoi {
    private String sbd;
    private Diem diemThi;
    private Double diemUuTien;
    private String khoiXetTuyen;
    private String queQuan;

    public ThiSinh() {
        super();
        this.diemThi = new Diem();
    }

    public ThiSinh(String sbd, String hoTen, LocalDate ngaySinh, String queQuan, Diem diemThi, Double diemUuTien, String khoiXetTuyen) {
        this.sbd = sbd;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.queQuan = queQuan;
        this.diemThi = diemThi;
        this.diemUuTien = (diemUuTien != null) ? diemUuTien : 0.0;
        this.khoiXetTuyen = khoiXetTuyen;
    }

    public double getTongDiemXetTuyen(String khoi) {
        if (diemThi == null) return 0.0;
        double tongBaMon = diemThi.tinhTongTheoKhoi(khoi);
        if (tongBaMon == 0) return 0.0;
        return tongBaMon + (diemUuTien != null ? diemUuTien : 0.0);
    }

    public String getSbd() { return sbd; }
    public void setSbd(String sbd) { this.sbd = sbd; }

    public Diem getDiemThi() { return diemThi; }
    public void setDiemThi(Diem diemThi) { this.diemThi = diemThi; }

    public Double getDiemUuTien() { return diemUuTien; }
    public void setDiemUuTien(Double diemUuTien) { this.diemUuTien = diemUuTien; }

    public String getKhoiXetTuyen() { return khoiXetTuyen; }
    public void setKhoiXetTuyen(String khoiXetTuyen) { this.khoiXetTuyen = khoiXetTuyen; }

    public String getQueQuan() { return queQuan; }
    public void setQueQuan(String queQuan) { this.queQuan = queQuan; }
}