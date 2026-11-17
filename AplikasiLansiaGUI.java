import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AplikasiLansiaGUI extends JFrame {
    private String[][] dataLansia = new String[200][8];
    private int jumlahLansia = 0;
    private final String FILE_NAME = "database_lansia.txt";
    private JPanel panelKontenKanan;
    private JTextField txtID, txtNama, txtUmur, txtAlamat, txtGulaDarah, txtJadwal, txtObat;
    private JComboBox<String> cbKondisi;
    private DefaultTableModel tableModel;
    private JLabel lblTotal, lblNormal, lblPreHip, lblHipertensi;
    private JTextArea txtStatistik;
    private PieChartPanel diagramLingkaran;
    private DefaultTableModel tableModelHealth;
    private Timer timerAnimasi;
    private JPanel halamanBaru, halamanLama;
    private int posisiXBaru = 0;
    private final int KECEPATAN_SLIDE = 40; 

    public AplikasiLansiaGUI() {
        setTitle("Sistem Monitoring Kesehatan Lansia");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            java.net.URL iconURL = getClass().getResource("/Icon.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                setIconImage(icon.getImage());
            } else {
                ImageIcon icon = new ImageIcon("Icon.png");
                setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            System.out.println("Gagal memuat ikon: " + e.getMessage());
        }

        muatDataDariFile();

        // 1. SIDEBAR NAVIGASI (KIRI)
        JPanel panelSidebar = new JPanel();
        panelSidebar.setBackground(new Color(13, 27, 42));
        panelSidebar.setPreferredSize(new Dimension(220, 650));
        panelSidebar.setLayout(new GridBagLayout()); 

        GridBagConstraints gbcSidebar = new GridBagConstraints();
        gbcSidebar.fill = GridBagConstraints.BOTH;
        gbcSidebar.gridx = 0; gbcSidebar.weightx = 1.0;

        // PANEL JUDUL
        JPanel panelJudulSidebar = new JPanel(new BorderLayout());
        panelJudulSidebar.setBackground(new Color(28, 59, 102)); 
        JLabel lblMenu = new JLabel("Smartcare Lansia", JLabel.CENTER);
        lblMenu.setForeground(Color.WHITE);
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 16)); 
        panelJudulSidebar.add(lblMenu, BorderLayout.CENTER);
        
        gbcSidebar.gridy = 0; gbcSidebar.weighty = 0.15;
        panelSidebar.add(panelJudulSidebar, gbcSidebar);

        // PANEL UNTUK TOMBOL-TOMBOL NAVIGASI (5 baris menu)
        JPanel panelTombol = new JPanel(new GridLayout(5, 1, 0, 10));
        panelTombol.setBackground(new Color(13, 27, 42));
        panelTombol.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton btnDash = new JButton("🏠 Dashboard");
        JButton btnInput = new JButton("📥 Input Data");
        JButton btnData = new JButton("📋 Daftar Nama");
        JButton btnHealth = new JButton("🔔 Health Check-up"); 
        JButton btnStat = new JButton("📊 Statistik & Umur");

        panelTombol.add(btnDash); 
        panelTombol.add(btnInput);
        panelTombol.add(btnData); 
        panelTombol.add(btnHealth); 
        panelTombol.add(btnStat);
        
        gbcSidebar.gridy = 1; gbcSidebar.weighty = 0.85;
        panelSidebar.add(panelTombol, gbcSidebar);

        add(panelSidebar, BorderLayout.WEST);

        // 2. KONTEN UTAMA (KANAN)
        panelKontenKanan = new JPanel(null);
        panelKontenKanan.setBackground(new Color(217, 234, 253));

        halamanBaru = buatHalamanDashboard();
        halamanBaru.setBounds(0, 0, 860, 610); 
        panelKontenKanan.add(halamanBaru);
        add(panelKontenKanan, BorderLayout.CENTER);

        // --- AKSI TOMBOL NAVIGASI DENGAN NOTIFIKASI OTOMATIS ---
        btnDash.addActionListener(e -> {
            mulaiAnimasiGeser(buatHalamanDashboard());
            cekPengingatJadwal();
        });
        
        btnInput.addActionListener(e -> {
            mulaiAnimasiGeser(buatHalamanInput());
        });
        
        btnData.addActionListener(e -> {
            mulaiAnimasiGeser(buatHalamanData());
            cekPengingatJadwal(); 
        });
        
        btnHealth.addActionListener(e -> {
            mulaiAnimasiGeser(buatHalamanHealthCheckup()); 
            cekPengingatJadwal(); 
        });
        
        btnStat.addActionListener(e -> {
            mulaiAnimasiGeser(buatHalamanStatistik());
            cekPengingatJadwal(); 
        });

        perbaruiData();
        cekPengingatJadwal();
    }

    private void mulaiAnimasiGeser(JPanel panelTujuan) {
        if (timerAnimasi != null && timerAnimasi.isRunning()) {
            timerAnimasi.stop(); 
        }

        halamanLama = halamanBaru;
        halamanBaru = panelTujuan;

        int lebarPanel = panelKontenKanan.getWidth();
        int tinggiPanel = panelKontenKanan.getHeight();
        if (lebarPanel == 0) lebarPanel = 860;  
        if (tinggiPanel == 0) tinggiPanel = 610;

        posisiXBaru = lebarPanel;
        halamanBaru.setBounds(posisiXBaru, 0, lebarPanel, tinggiPanel);
        
        panelKontenKanan.add(halamanBaru);
        panelKontenKanan.revalidate();

        final int currentLebar = lebarPanel;
        final int currentTinggi = tinggiPanel;

        timerAnimasi = new Timer(10, e -> {
            posisiXBaru -= KECEPATAN_SLIDE; 

            if (posisiXBaru <= 0) {
                posisiXBaru = 0;
                timerAnimasi.stop(); 
                if (halamanLama != null) {
                    panelKontenKanan.remove(halamanLama); 
                }
            }

            halamanBaru.setBounds(posisiXBaru, 0, currentLebar, currentTinggi);
            if (halamanLama != null) {
                halamanLama.setBounds(posisiXBaru - currentLebar, 0, currentLebar, currentTinggi);
            }
            panelKontenKanan.repaint();
        });
        timerAnimasi.start();
        
        perbaruiData();
    }

    private JPanel buatHalamanDashboard() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(217, 234, 253)); 
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // --- ROW 1: TOTAL PASIEN ---
        JPanel cardTotal = new JPanel(new BorderLayout());
        cardTotal.setBackground(new Color(28, 59, 102)); 
        cardTotal.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitleTotal = new JLabel("TOTAL PASIEN", JLabel.LEFT);
        lblTitleTotal.setForeground(new Color(220, 231, 245));
        lblTitleTotal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cardTotal.add(lblTitleTotal, BorderLayout.NORTH);

        lblTotal = new JLabel("", JLabel.LEFT);
        cardTotal.add(lblTotal, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.weighty = 0.4;
        p.add(cardTotal, gbc);

        // --- ROW 2: KOTAK TENSI NORMAL ---
        JPanel cardNormal = new JPanel(new BorderLayout());
        cardNormal.setBackground(Color.WHITE); 
        cardNormal.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitleNormal = new JLabel("TENSI NORMAL", JLabel.LEFT);
        lblTitleNormal.setForeground(Color.GRAY);
        lblTitleNormal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cardNormal.add(lblTitleNormal, BorderLayout.NORTH);

        lblNormal = new JLabel("", JLabel.LEFT);
        cardNormal.add(lblNormal, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1; gbc.weightx = 0.5; gbc.weighty = 0.3;
        p.add(cardNormal, gbc);

        // --- ROW 2: KOTAK PRE-HIPERTENSI ---
        JPanel cardPreHip = new JPanel(new BorderLayout());
        cardPreHip.setBackground(Color.WHITE); 
        cardPreHip.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitlePre = new JLabel("PRE-HIPERTENSI", JLabel.LEFT);
        lblTitlePre.setForeground(Color.GRAY);
        lblTitlePre.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cardPreHip.add(lblTitlePre, BorderLayout.NORTH);

        lblPreHip = new JLabel("", JLabel.LEFT);
        cardPreHip.add(lblPreHip, BorderLayout.CENTER);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.gridwidth = 1; gbc.weightx = 0.5; gbc.weighty = 0.3;
        p.add(cardPreHip, gbc);

        // --- ROW 3: KOTAK HIPERTENSI ---
        JPanel cardHipertensi = new JPanel(new BorderLayout());
        cardHipertensi.setBackground(Color.WHITE); 
        cardHipertensi.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitleHip = new JLabel("HIPERTENSI", JLabel.LEFT);
        lblTitleHip.setForeground(Color.GRAY);
        lblTitleHip.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cardHipertensi.add(lblTitleHip, BorderLayout.NORTH);

        lblHipertensi = new JLabel("", JLabel.LEFT);
        cardHipertensi.add(lblHipertensi, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.weighty = 0.3;
        p.add(cardHipertensi, gbc);

        return p;
    }

    private JPanel buatHalamanInput() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(217, 234, 253)); 
        
        JPanel form = new JPanel(new GridLayout(9, 2, 10, 10));
        form.setBorder(BorderFactory.createTitledBorder("Form Rekam Medis Lansia"));

        form.add(new JLabel("ID Pasien:")); txtID = new JTextField(15); form.add(txtID);
        form.add(new JLabel("Nama Lengkap:")); txtNama = new JTextField(15); form.add(txtNama);
        form.add(new JLabel("Umur (Tahun):")); txtUmur = new JTextField(15); form.add(txtUmur);
        form.add(new JLabel("Alamat:")); txtAlamat = new JTextField(15); form.add(txtAlamat); 
        form.add(new JLabel("Gula Darah (mg/dL):")); txtGulaDarah = new JTextField(15); form.add(txtGulaDarah); 
        form.add(new JLabel("Jadwal Kontrol (dd-MM-yyyy):")); txtJadwal = new JTextField(15); form.add(txtJadwal);
        form.add(new JLabel("Nama Obat:")); txtObat = new JTextField(15); form.add(txtObat);
        form.add(new JLabel("Kondisi Tensi:")); 
        cbKondisi = new JComboBox<>(new String[]{"Normal", "Pre-Hipertensi", "Hipertensi"});
        form.add(cbKondisi);

        JButton btnSimpan = new JButton("Simpan ke Transaksi");
        form.add(btnSimpan); p.add(form);

        btnSimpan.addActionListener(e -> {
            if(txtID.getText().isEmpty() || txtNama.getText().isEmpty()) return;
            dataLansia[jumlahLansia][0] = txtID.getText();
            dataLansia[jumlahLansia][1] = txtNama.getText();
            dataLansia[jumlahLansia][2] = txtUmur.getText();
            dataLansia[jumlahLansia][3] = txtAlamat.getText();     
            dataLansia[jumlahLansia][4] = txtGulaDarah.getText();  
            dataLansia[jumlahLansia][5] = txtJadwal.getText();
            dataLansia[jumlahLansia][6] = txtObat.getText();
            dataLansia[jumlahLansia][7] = (String) cbKondisi.getSelectedItem();
            jumlahLansia++;
            simpanDataKeFile();
            perbaruiData();
            
            txtID.setText(""); txtNama.setText(""); txtUmur.setText(""); 
            txtAlamat.setText(""); txtGulaDarah.setText(""); 
            txtJadwal.setText(""); txtObat.setText("");
            
            JOptionPane.showMessageDialog(this, "Data Berhasil Disimpan!");
        });
        return p;
    }

    private JPanel buatHalamanData() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(new Color(217, 234, 253)); 
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        tableModel = new DefaultTableModel(
            new String[]{"ID", "Nama", "Umur", "Alamat", "Gula Darah", "Jadwal", "Obat", "Tensi"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        JTable tabelUtama = new JTable(tableModel);
        p.add(new JScrollPane(tabelUtama), BorderLayout.CENTER);

        // --- SUB PANEL UNTUK AKSI EDIT & HAPUS ---
        JPanel panelAksi = new JPanel(new GridLayout(1, 2, 15, 0));
        panelAksi.setBackground(new Color(217, 234, 253));

        JButton btnEdit = new JButton("✏️ Edit Data Terpilih");
        btnEdit.setBackground(new Color(49, 130, 206));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton btnHapus = new JButton("❌ Hapus Data Terpilih");
        btnHapus.setBackground(new Color(229, 62, 62));
        btnHapus.setForeground(Color.WHITE);
        btnHapus.setFont(new Font("Segoe UI", Font.BOLD, 12));

        panelAksi.add(btnEdit);
        panelAksi.add(btnHapus);
        p.add(panelAksi, BorderLayout.SOUTH);

        // --- AKSI FITUR EDIT DATA ---
        btnEdit.addActionListener(e -> {
            int barisTerpilih = tabelUtama.getSelectedRow();
            if (barisTerpilih == -1) {
                JOptionPane.showMessageDialog(this, "Silakan pilih baris data lansia yang ingin diubah terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            tampilkanDialogEdit(barisTerpilih);
        });

        // --- AKSI FITUR HAPUS DATA ---
        btnHapus.addActionListener(e -> {
            int barisTerpilih = tabelUtama.getSelectedRow();
            if (barisTerpilih == -1) {
                JOptionPane.showMessageDialog(this, "Silakan pilih baris data lansia yang ingin dihapus terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int konfirmasi = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus data lansia terpilih?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (konfirmasi == JOptionPane.YES_OPTION) {
                for (int i = barisTerpilih; i < jumlahLansia - 1; i++) {
                    dataLansia[i] = dataLansia[i + 1];
                }
                dataLansia[jumlahLansia - 1] = new String[8]; 
                jumlahLansia--;

                simpanDataKeFile();
                perbaruiData();
                JOptionPane.showMessageDialog(this, "Data lansia berhasil dihapus!");
            }
        });

        return p;
    }

    private void tampilkanDialogEdit(int rowIndex) {
        JDialog dialog = new JDialog(this, "Ubah Rekam Medis Lansia", true);
        dialog.setSize(450, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());

        JPanel form = new JPanel(new GridLayout(8, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField dTxtID = new JTextField(dataLansia[rowIndex][0]);
        dTxtID.setEditable(false); // ID biasanya primary key, dikunci agar tidak bentrok
        JTextField dTxtNama = new JTextField(dataLansia[rowIndex][1]);
        JTextField dTxtUmur = new JTextField(dataLansia[rowIndex][2]);
        JTextField dTxtAlamat = new JTextField(dataLansia[rowIndex][3]);
        JTextField dTxtGula = new JTextField(dataLansia[rowIndex][4]);
        JTextField dTxtJadwal = new JTextField(dataLansia[rowIndex][5]);
        JTextField dTxtObat = new JTextField(dataLansia[rowIndex][6]);
        
        JComboBox<String> dCbKondisi = new JComboBox<>(new String[]{"Normal", "Pre-Hipertensi", "Hipertensi"});
        dCbKondisi.setSelectedItem(dataLansia[rowIndex][7]);

        form.add(new JLabel("ID Pasien (Tetap):")); form.add(dTxtID);
        form.add(new JLabel("Nama Lengkap:")); form.add(dTxtNama);
        form.add(new JLabel("Umur (Tahun):")); form.add(dTxtUmur);
        form.add(new JLabel("Alamat:")); form.add(dTxtAlamat);
        form.add(new JLabel("Gula Darah (mg/dL):")); form.add(dTxtGula);
        form.add(new JLabel("Jadwal Kontrol:")); form.add(dTxtJadwal);
        form.add(new JLabel("Nama Obat:")); form.add(dTxtObat);
        form.add(new JLabel("Kondisi Tensi:")); form.add(dCbKondisi);

        JButton btnUpdate = new JButton("💾 Simpan Perubahan");
        btnUpdate.setBackground(new Color(34, 84, 61));
        btnUpdate.setForeground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH;
        dialog.add(form, gbc);

        gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.insets = new Insets(0, 0, 15, 0);
        dialog.add(btnUpdate, gbc);

        btnUpdate.addActionListener(ev -> {
            if(dTxtNama.getText().isEmpty() || dTxtUmur.getText().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Nama dan Umur tidak boleh kosong!");
                return;
            }
            dataLansia[rowIndex][1] = dTxtNama.getText();
            dataLansia[rowIndex][2] = dTxtUmur.getText();
            dataLansia[rowIndex][3] = dTxtAlamat.getText();
            dataLansia[rowIndex][4] = dTxtGula.getText();
            dataLansia[rowIndex][5] = dTxtJadwal.getText();
            dataLansia[rowIndex][6] = dTxtObat.getText();
            dataLansia[rowIndex][7] = (String) dCbKondisi.getSelectedItem();

            simpanDataKeFile();
            perbaruiData();
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Data lansia berhasil diperbarui!");
        });

        dialog.setVisible(true);
    }

    private JPanel buatHalamanHealthCheckup() {
        JPanel p = new JPanel(new BorderLayout(15, 15));
        p.setBackground(new Color(217, 234, 253));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel panelKonten = new JPanel(new BorderLayout());
        panelKonten.setBackground(Color.WHITE);
        panelKonten.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(28, 59, 102), 2),
            " 🔔 Tabel Jadwal Health Check-up Lansia Hari Ini ",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(28, 59, 102)
        ));

        tableModelHealth = new DefaultTableModel(
            new String[]{"ID Pasien", "Nama Lansia", "Hari", "Tanggal", "Bulan", "Tahun", "Jam Kontrol", "Nama Obat"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        JTable tableHealth = new JTable(tableModelHealth);
        tableHealth.setRowHeight(25);
        tableHealth.getTableHeader().setBackground(new Color(28, 59, 102));
        tableHealth.getTableHeader().setForeground(Color.WHITE);
        tableHealth.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        panelKonten.add(new JScrollPane(tableHealth), BorderLayout.CENTER);
        p.add(panelKonten, BorderLayout.CENTER);

        return p;
    }

    private JPanel buatHalamanStatistik() {
        JPanel p = new JPanel(new GridLayout(1, 2, 15, 0));
        p.setBackground(new Color(217, 234, 253)); 
        p.setBorder(BorderFactory.createTitledBorder("Analisis Penyakit Terbanyak & Distribusi Usia"));

        txtStatistik = new JTextArea();
        txtStatistik.setFont(new Font("Monospaced", Font.PLAIN, 14));
        txtStatistik.setEditable(false);
        p.add(new JScrollPane(txtStatistik));

        JPanel panelGrafik = new JPanel(new BorderLayout());
        panelGrafik.setBackground(Color.WHITE);
        panelGrafik.setBorder(BorderFactory.createTitledBorder("Visualisasi Grafik Tensi"));
        
        diagramLingkaran = new PieChartPanel();
        panelGrafik.add(diagramLingkaran, BorderLayout.CENTER);
        p.add(panelGrafik);

        return p;
    }

    private void perbaruiData() {
        if(tableModel == null) return;
        tableModel.setRowCount(0);
        
        if(tableModelHealth != null) {
            tableModelHealth.setRowCount(0);
        }

        int n = 0, ph = 0, h = 0;
        int u60 = 0, u70 = 0;

        LocalDate hariIni = LocalDate.now();
        DateTimeFormatter formatDatabase = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String tanggalHariIniStr = hariIni.format(formatDatabase);

        Locale localeID = new Locale("id", "ID");
        String stringHari = hariIni.format(DateTimeFormatter.ofPattern("EEEE", localeID));
        String stringTanggal = hariIni.format(DateTimeFormatter.ofPattern("dd", localeID));
        String stringBulan = hariIni.format(DateTimeFormatter.ofPattern("MMMM", localeID));
        String stringTahun = hariIni.format(DateTimeFormatter.ofPattern("yyyy", localeID));

        String stringJam = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        for (int i = 0; i < jumlahLansia; i++) {
            tableModel.addRow(dataLansia[i]);
            if (dataLansia[i][7].equals("Normal")) n++;
            else if (dataLansia[i][7].equals("Pre-Hipertensi")) ph++;
            else if (dataLansia[i][7].equals("Hipertensi")) h++;

            try {
                int umur = Integer.parseInt(dataLansia[i][2].trim());
                if (umur <= 70) u60++; else u70++;
            } catch (Exception e) {}

            String jadwalPasien = dataLansia[i][5];
            if (jadwalPasien != null && jadwalPasien.trim().equals(tanggalHariIniStr)) {
                if (tableModelHealth != null) {
                    tableModelHealth.addRow(new Object[]{
                        dataLansia[i][0], 
                        dataLansia[i][1], 
                        stringHari,       
                        stringTanggal,    
                        stringBulan,      
                        stringTahun,      
                        stringJam + " WIB", 
                        dataLansia[i][6]  
                    });
                }
            }
        }

        if(lblTotal != null) lblTotal.setText("<html><font color='white' size='6'><b>" + jumlahLansia + " Orang</b></font></html>");
        if(lblNormal != null) lblNormal.setText("<html><font color='#2c3e50' size='6'><b>" + n + " Orang</b></font></html>");
        if(lblPreHip != null) lblPreHip.setText("<html><font color='#2c3e50' size='6'><b>" + ph + " Orang</b></font></html>");
        if(lblHipertensi != null) lblHipertensi.setText("<html><font color='#2c3e50' size='6'><b>" + h + " Orang</b></font></html>");

        if (diagramLingkaran != null) diagramLingkaran.setValues(n, ph, h);

        if (txtStatistik != null) {
            txtStatistik.setText(
                "==================================================\n" +
                "       LAPORAN STATISTIK KESEHATAN LANSIA\n" +
                "==================================================\n\n" +
                " A. SEBARAN PENYAKIT (KONDISI TENSI):\n" +
                "    1. Tensi Normal       : " + n + " Pasien\n" +
                "    2. Pre-Hipertensi     : " + ph + " Pasien\n" +
                "    3. Hipertensi Akut    : " + h + " Pasien\n\n" +
                " B. DISTRIBUSI UMUR:\n" +
                "    1. Rentang Usia 60 - 70 Tahun : " + u60 + " Pasien\n" +
                "    2. Rentang Usia Di Atas 70 Tahun: " + u70 + " Pasien\n\n" +
                " Kesimpulan Penyakit Terbanyak: " +
                ((h >= n && h >= ph) ? "Hipertensi" : (ph >= n && ph >= h) ? "Pre-Hipertensi" : "Normal Tensi")
            );
        }
    }

    private void cekPengingatJadwal() {
        LocalDate hariIni = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String tanggalHariIni = hariIni.format(formatter);

        int countPasien = 0;
        for (int i = 0; i < jumlahLansia; i++) {
            String jadwalPasien = dataLansia[i][5]; 
            if (jadwalPasien != null && jadwalPasien.trim().equals(tanggalHariIni)) {
                countPasien++;
            }
        }

        if (countPasien > 0) {
            JOptionPane.showMessageDialog(this,
                "🔔 HEALTH CHECK-UP LANSIA HARI INI (" + tanggalHariIni + ") 🔔\n\n" +
                "Ada " + countPasien + " pasien yang harus kontrol hari ini.\n" +
                "Silakan buka menu 'Health Check-up' untuk melihat rincian tabel waktu.",
                "Notifikasi Kontrol Lansia",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void simpanDataKeFile() {
        try (PrintWriter w = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < jumlahLansia; i++) {
                w.println(dataLansia[i][0] + ";" + dataLansia[i][1] + ";" + dataLansia[i][2] + ";" +
                          dataLansia[i][3] + ";" + dataLansia[i][4] + ";" + dataLansia[i][5] + ";" + 
                          dataLansia[i][6] + ";" + dataLansia[i][7]);
            }
        } catch (Exception e) {}
    }

    private void muatDataDariFile() {
        File f = new File(FILE_NAME);
        if (!f.exists()) return;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String b; jumlahLansia = 0;
            while ((b = r.readLine()) != null) {
                String[] d = b.split(";");
                if (d.length == 8) dataLansia[jumlahLansia++] = d;
            }
        } catch (Exception e) {}
    }

    class PieChartPanel extends JPanel {
        private int normal, preHip, hipertensi;

        public void setValues(int normal, int preHip, int hipertensi) {
            this.normal = normal; this.preHip = preHip; this.hipertensi = hipertensi;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int total = normal + preHip + hipertensi;
            int x = 30, y = 40, width = 200, height = 200;

            if (total == 0) {
                g2.setColor(Color.LIGHT_GRAY); g2.fillArc(x, y, width, height, 0, 360);
                g2.setColor(Color.BLACK); g2.drawString("Belum ada data pasien.", x + 30, y + 100);
                return;
            }

            int sudutNormal = (int) Math.round((double) normal / total * 360);
            int sudutPreHip = (int) Math.round((double) preHip / total * 360);
            int sudutHipertensi = 360 - sudutNormal - sudutPreHip;

            int sudutMulai = 0;
            g2.setColor(new Color(72, 187, 120)); g2.fillArc(x, y, width, height, sudutMulai, sudutNormal);
            sudutMulai += sudutNormal;
            g2.setColor(new Color(237, 137, 54)); g2.fillArc(x, y, width, height, sudutMulai, sudutPreHip);
            sudutMulai += sudutPreHip;
            g2.setColor(new Color(229, 62, 62)); g2.fillArc(x, y, width, height, sudutMulai, sudutHipertensi);

            int lx = x + width + 30, ly = y + 40;
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(new Color(72, 187, 120)); g2.fillRect(lx, ly, 15, 12);
            g2.setColor(Color.BLACK); g2.drawString("Normal", lx + 25, ly + 11);
            ly += 30;
            g2.setColor(new Color(237, 137, 54)); g2.fillRect(lx, ly, 15, 12);
            g2.setColor(Color.BLACK); g2.drawString("Pre-Hipertensi", lx + 25, ly + 11);
            ly += 30;
            g2.setColor(new Color(229, 62, 62)); g2.fillRect(lx, ly, 15, 12);
            g2.setColor(Color.BLACK); g2.drawString("Hipertensi", lx + 25, ly + 11);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AplikasiLansiaGUI().setVisible(true));
    }
}