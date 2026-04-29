package com.thai.unipath.model;

public class Diem {
    // Khai báo full các môn theo chương trình mới
    private Double toan, van, anh;
    private Double ly, hoa, sinh;
    private Double su, dia, gdkt;
    private Double tin, congNghe;

    public Diem() {}

    /**
     * Hàm tính tổng điểm theo khối siêu tốc
     * Nếu không thi môn đó (null) thì tự động coi là 0 điểm để không bị lỗi
     */
    public double tinhTongTheoKhoi(String khoi) {
        if (khoi == null) return 0.0;

        double t = (toan != null) ? toan : 0.0;
        double v = (van != null) ? van : 0.0;
        double a = (anh != null) ? anh : 0.0;
        double l = (ly != null) ? ly : 0.0;
        double h = (hoa != null) ? hoa : 0.0;
        double s = (sinh != null) ? sinh : 0.0;
        double suDiem = (su != null) ? su : 0.0;
        double d = (dia != null) ? dia : 0.0;

        double tong = 0.0;
        switch (khoi.toUpperCase()) {
            case "A00": tong = t + l + h; break;
            case "A01": tong = t + l + a; break;
            case "B00": tong = t + h + s; break;
            case "C00": tong = v + suDiem + d; break;
            case "D01": tong = t + v + a; break;
            default: tong = 0.0; // Các khối khác ông có thể thêm vào sau
        }
        return tong;
    }

    // ================= GETTER & SETTER =================
    public Double getToan() { return toan; }
    public void setToan(Double toan) { this.toan = toan; }

    public Double getVan() { return van; }
    public void setVan(Double van) { this.van = van; }

    public Double getAnh() { return anh; }
    public void setAnh(Double anh) { this.anh = anh; }

    public Double getLy() { return ly; }
    public void setLy(Double ly) { this.ly = ly; }

    public Double getHoa() { return hoa; }
    public void setHoa(Double hoa) { this.hoa = hoa; }

    public Double getSinh() { return sinh; }
    public void setSinh(Double sinh) { this.sinh = sinh; }

    public Double getSu() { return su; }
    public void setSu(Double su) { this.su = su; }

    public Double getDia() { return dia; }
    public void setDia(Double dia) { this.dia = dia; }

    public Double getGdkt() { return gdkt; }
    public void setGdkt(Double gdkt) { this.gdkt = gdkt; }

    public Double getTin() { return tin; }
    public void setTin(Double tin) { this.tin = tin; }

    public Double getCongNghe() { return congNghe; }
    public void setCongNghe(Double congNghe) { this.congNghe = congNghe; }
}