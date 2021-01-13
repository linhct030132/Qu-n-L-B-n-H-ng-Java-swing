/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qlbh.view;

import UserInterFace.Detail;
import java.awt.Color;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

/**
 *
 * @author Steven
 */
public class NhapHang extends javax.swing.JFrame {

    private Connection conn = null;
    private PreparedStatement pst = null;
    private ResultSet rs = null;

    private String sql = "SELECT * FROM PhieuNhap";
    private boolean Add = false, Change = false;

    private Detail detail;

    /**
     * Creates new form NhapHang
     */
    public NhapHang(Detail d) {
        initComponents();
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        detail = new Detail(d);
        jlbStatus.setForeground(Color.red);

        connection();
        Load(sql);
        disabled();
    }

    private void connection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=QLBH1;user=sa;password=123");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void Load(String sql) {
        tblNhapHang.removeAll();
        try {
            String[] arr = {"Mã Nhập Hàng", "Mã Sản Phẩm", "Tên Sản Phẩm", "Loại Sản Phẩm", "Nhà Cung Cấp", "Số Lượng Nhập", "Ngày Lập" ,"Đơn Giá", "Tổng Tiền"};
            DefaultTableModel modle = new DefaultTableModel(arr, 0);
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                Vector vector = new Vector();
                vector.add(rs.getString("MaNhapHang").trim());
                vector.add(rs.getString("MaHang").trim());
                vector.add(rs.getString("TenHang").trim());
                vector.add(rs.getString("LoaiSanPham").trim());
                vector.add(rs.getString("NhaCungCap").trim());
                vector.add(rs.getInt("SoLuong"));
                vector.add(new SimpleDateFormat("dd/MM/yyyy").format(rs.getDate("NgayNhap")));
                vector.add(rs.getString("DonGia").trim());
                vector.add(rs.getString("TongTien"));
                modle.addRow(vector);
            }
            tblNhapHang.setModel(modle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void disabled() {
        txtMaNhap.setEnabled(false);
        txtTenhang.setEnabled(false);
        txtMaSP.setEnabled(false);
        cbxLoaiSP.setEnabled(false);
        cbxNhaCungCap.setEnabled(false);
        txtSoLuong.setEnabled(false);
        jdcNgayNhap.setEnabled(false);
        txtDonGia.setEnabled(false);
        txtTongTien.setEnabled(false);
    }

    public void enabled() {
        txtMaNhap.setEnabled(true);
        txtTenhang.setEnabled(true);
        cbxLoaiSP.setEnabled(true);
        txtMaSP.setEnabled(true);
        cbxNhaCungCap.setEnabled(true);
        txtSoLuong.setEnabled(true);
        jdcNgayNhap.setEnabled(true);
        txtDonGia.setEnabled(true);
        jlbStatus.setText("Trạng Thái!");
    }

    public void refresh() {
        txtMaNhap.setText("");
        txtTenhang.setText("");
        txtMaSP.setText("");
        btnAdd.setEnabled(true);
        btnChange.setEnabled(false);
        btnDelete.setEnabled(false);
        btnSave.setEnabled(false);
        Add = false;
        Change = false;
        txtSoLuong.setText("");
        txtDonGia.setText("");
        txtTongTien.setText("");
        disabled();
    }

    public void LoadClassify() {
        cbxLoaiSP.removeAllItems();
        String sql = "SELECT * FROM Classify";
        try {
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                this.cbxLoaiSP.addItem(rs.getString("Classify").trim());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void LoadProducer() {
        cbxNhaCungCap.removeAllItems();
        String sql = "SELECT * FROM Producer";
        try {
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                this.cbxNhaCungCap.addItem(rs.getString("ProducerName").trim());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean checkNull() {
        boolean kq = true;
        if (String.valueOf(this.txtMaNhap.getText()).length() == 0) {
            jlbStatus.setText("Bạn chưa nhập mã cho nhập hàng!");
            return false;
        }
        if (String.valueOf(this.txtTenhang.getText()).length() == 0) {
            jlbStatus.setText("Bạn chưa nhập Tên sản phẩm!");
            return false;
        }
        if (String.valueOf(this.txtSoLuong.getText()).length() == 0) {
            jlbStatus.setText("Bạn chưa nhập số lượng sản phẩm hiện có!");
            return false;
        }

        if (String.valueOf(this.txtDonGia.getText()).length() == 0) {
            jlbStatus.setText("Bạn chưa nhập giá cho sản phẩm!");
            return false;
        }

        return kq;
    }

    public void setTongTien() {
        DecimalFormat formatter = new DecimalFormat("###,###,###");

        txtSoLuong.setText(cutChar(txtSoLuong.getText()));

        int soluong = Integer.parseInt(txtSoLuong.getText().toString());
        String[] s = txtDonGia.getText().split("\\s");

        txtTongTien.setText(formatter.format(convertedToNumbers(s[0]) * soluong) );
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        btnBackHome = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jpnThongTin = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        txtMaNhap = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtSoLuong = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jdcNgayNhap = new com.toedter.calendar.JDateChooser();
        jLabel7 = new javax.swing.JLabel();
        txtTongTien = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtDonGia = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtTenhang = new javax.swing.JTextField();
        cbxLoaiSP = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        cbxNhaCungCap = new javax.swing.JComboBox<>();
        btnClassify = new javax.swing.JButton();
        btnNSX = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        txtMaSP = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblNhapHang = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        btnRefresh = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnChange = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jlbStatus = new javax.swing.JLabel();

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jLabel10.setText("jLabel10");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnBackHome.setText("Hệ Thống");
        btnBackHome.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnBackHomeMouseClicked(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Times New Roman", 0, 28)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Quản Lý Nhập Hàng");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(304, 304, 304)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 558, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(23, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jpnThongTin.setBorder(javax.swing.BorderFactory.createTitledBorder("Thông Tin Nhập Hàng"));

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 17)); // NOI18N
        jLabel3.setText("Mã Nhập Hàng:");

        jLabel4.setFont(new java.awt.Font("Dialog", 1, 17)); // NOI18N
        jLabel4.setText("Tên Hàng:");

        jLabel5.setFont(new java.awt.Font("Dialog", 1, 17)); // NOI18N
        jLabel5.setText("Số Lượng:");

        txtSoLuong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSoLuongActionPerformed(evt);
            }
        });
        txtSoLuong.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSoLuongKeyReleased(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Dialog", 1, 17)); // NOI18N
        jLabel6.setText("Ngày Nhập:");

        jdcNgayNhap.setDateFormatString("dd/MM/yyyy");
        jdcNgayNhap.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jdcNgayNhapComponentShown(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Dialog", 1, 17)); // NOI18N
        jLabel7.setText("Tổng Tiền:");

        jLabel8.setFont(new java.awt.Font("Dialog", 1, 17)); // NOI18N
        jLabel8.setText("Đơn Giá:");

        jLabel2.setFont(new java.awt.Font("Dialog", 1, 17)); // NOI18N
        jLabel2.setText("Loại Sản Phẩm:");

        cbxLoaiSP.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " " }));
        cbxLoaiSP.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                cbxLoaiSPPopupMenuWillBecomeInvisible(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        jLabel9.setFont(new java.awt.Font("Dialog", 1, 17)); // NOI18N
        jLabel9.setText("Nhà Cung Cấp:");

        cbxNhaCungCap.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                cbxNhaCungCapPopupMenuWillBecomeInvisible(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        btnClassify.setText("...");
        btnClassify.setEnabled(false);
        btnClassify.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClassifyActionPerformed(evt);
            }
        });

        btnNSX.setText("...");
        btnNSX.setEnabled(false);
        btnNSX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNSXActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Dialog", 1, 17)); // NOI18N
        jLabel11.setText("Mã Sản Phẩm;");

        txtMaSP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMaSPActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpnThongTinLayout = new javax.swing.GroupLayout(jpnThongTin);
        jpnThongTin.setLayout(jpnThongTinLayout);
        jpnThongTinLayout.setHorizontalGroup(
            jpnThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpnThongTinLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jpnThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel11)
                    .addComponent(jLabel4))
                .addGap(25, 25, 25)
                .addGroup(jpnThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtTenhang, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                    .addComponent(txtMaNhap, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                    .addComponent(txtMaSP))
                .addGap(18, 18, 18)
                .addGroup(jpnThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpnThongTinLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(4, 4, 4))
                    .addGroup(jpnThongTinLayout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(9, 9, 9)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(jpnThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cbxLoaiSP, 0, 155, Short.MAX_VALUE)
                    .addComponent(cbxNhaCungCap, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpnThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnNSX, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClassify, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(jpnThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addGap(19, 19, 19)
                .addGroup(jpnThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtSoLuong)
                    .addComponent(jdcNgayNhap, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE))
                .addGap(15, 15, 15)
                .addGroup(jpnThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addGap(18, 18, 18)
                .addGroup(jpnThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtDonGia, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                    .addComponent(txtTongTien))
                .addContainerGap())
        );
        jpnThongTinLayout.setVerticalGroup(
            jpnThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpnThongTinLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jpnThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpnThongTinLayout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(24, 24, 24)
                        .addComponent(jLabel7))
                    .addGroup(jpnThongTinLayout.createSequentialGroup()
                        .addGroup(jpnThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtMaNhap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(txtSoLuong, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(cbxLoaiSP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtDonGia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnClassify))
                        .addGap(18, 18, 18)
                        .addGroup(jpnThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jpnThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel6)
                                .addComponent(jLabel9)
                                .addComponent(cbxNhaCungCap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnNSX)
                                .addComponent(jLabel11)
                                .addComponent(txtMaSP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jdcNgayNhap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtTongTien, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jpnThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtTenhang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        tblNhapHang.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Mã Nhập Hàng", "Mã Sản Phẩm", "Tên Sản Phẩm", "Loại Sản Phẩm", "Nhà Cung Cấp", "Số Lượng", "Ngày Nhập", "Đơn Giá", "Tổng Tiền"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblNhapHang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblNhapHangMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblNhapHang);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Thao Tác"));

        btnRefresh.setBackground(new java.awt.Color(76, 175, 80));
        btnRefresh.setText("Reset");
        btnRefresh.setBorder(null);
        btnRefresh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnRefreshMouseClicked(evt);
            }
        });

        btnAdd.setBackground(new java.awt.Color(76, 175, 80));
        btnAdd.setText("+ Thêm");
        btnAdd.setBorder(null);
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnChange.setBackground(new java.awt.Color(76, 175, 80));
        btnChange.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/Change.png"))); // NOI18N
        btnChange.setText("Sửa");
        btnChange.setBorder(null);
        btnChange.setEnabled(false);
        btnChange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangeActionPerformed(evt);
            }
        });

        btnDelete.setBackground(new java.awt.Color(76, 175, 80));
        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/Delete.png"))); // NOI18N
        btnDelete.setText("Xóa");
        btnDelete.setBorder(null);
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnSave.setBackground(new java.awt.Color(76, 175, 80));
        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/Save.png"))); // NOI18N
        btnSave.setText("Lưu");
        btnSave.setBorder(null);
        btnSave.setEnabled(false);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(76, 175, 80));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/Print Sale.png"))); // NOI18N
        jButton2.setText("In Hóa Đơn");
        jButton2.setBorder(null);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(80, 80, 80)
                .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(87, 87, 87)
                .addComponent(btnChange, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(85, 85, 85)
                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(64, 64, 64)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(btnRefresh, javax.swing.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE)
                        .addGap(6, 6, 6))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnChange, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(15, 15, 15))
        );

        jlbStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlbStatus.setText("Trạng Thái!");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane2)
                            .addComponent(jpnThongTin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(btnBackHome, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(16, 16, 16))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jlbStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnBackHome, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jpnThongTin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jlbStatus)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRefreshMouseClicked
        refresh();
    }//GEN-LAST:event_btnRefreshMouseClicked

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        refresh();
        Add = true;
        btnAdd.setEnabled(false);
        btnSave.setEnabled(true);
        enabled();
        LoadClassify();
        LoadProducer();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangeActionPerformed
        Change = true;
        Add = false;
        btnAdd.setEnabled(false);
        btnDelete.setEnabled(false);
        btnChange.setEnabled(false);
        btnSave.setEnabled(true);
        enabled();
        LoadClassify();
        LoadProducer();
    }//GEN-LAST:event_btnChangeActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int Click = JOptionPane.showConfirmDialog(null, "Bạn có muốn xóa đơn đặt hàng hay không?", "Thông Báo", 2);
        if (Click == JOptionPane.YES_OPTION) {
            String sqlDelete = "DELETE FROM PhieuNhap WHERE MaNhapHang=? ";
            try {
                pst = conn.prepareStatement(sqlDelete);
                pst.setString(1, txtMaNhap.getText());
                pst.executeUpdate();
                jlbStatus.setText("Xóa đơn đặt hàng thành công!");
                disabled();
                refresh();
                Load(sql);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    public boolean Check() {
        boolean kq = true;
        String sqlCheck = "SELECT * FROM PhieuNhap";
        try {
            PreparedStatement pstCheck = conn.prepareStatement(sqlCheck);
            ResultSet rsCheck = pstCheck.executeQuery();
            while (rsCheck.next()) {
                if (this.txtMaNhap.getText().equals(rsCheck.getString("MaNhapHang").toString().trim())) {
                    return false;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return kq;
    }

    public void addPhieuNhap() {
        if (checkNull()) {
            String sqlInsert = "INSERT INTO PhieuNhap (MaNhapHang,MaHang,TenHang,LoaiSanPham,NhaCungCap,SoLuong,NgayNhap,DonGia,TongTien) "
                    + "VALUES(?,?,?,?,?,?,?,?,?)";
            try {
                pst = conn.prepareStatement(sqlInsert);
                pst.setString(1, (String) txtMaNhap.getText());
                pst.setString(2, txtMaSP.getText());
                pst.setString(3, (String) txtTenhang.getText());
                pst.setString(4, String.valueOf(this.cbxLoaiSP.getSelectedItem()));
                pst.setString(5, String.valueOf(this.cbxNhaCungCap.getSelectedItem()));
                pst.setInt(6, Integer.parseInt(txtSoLuong.getText()));
                pst.setDate(7, new java.sql.Date(jdcNgayNhap.getDate().getTime()));
                pst.setInt(8, Integer.parseInt(txtDonGia.getText()));
                pst.setString(9, this.txtTongTien.getText());
                pst.executeUpdate();
                jlbStatus.setText("Thêm sản phẩm thành công!");
                disabled();
                refresh();
                Load(sql);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void changeOrder() {
        int Click = tblNhapHang.getSelectedRow();
        TableModel model = tblNhapHang.getModel();

        if (checkNull()) {
            String sqlChange = "UPDATE PhieuNhap SET MaNhapHang=?,MaHang=?,TenHang=?,LoaiSanPham=?,NhaCungCap=?,SoLuong=?,NgayLap=?,DonGia=?,TongTien=?"
                    + "WHERE MaNhapHang='" + model.getValueAt(Click, 0).toString().trim() + "'";
            try {
                pst = conn.prepareStatement(sqlChange);

                pst.setString(1, txtMaNhap.getText());
                 pst.setString(2, this.txtMaSP.getText());
                pst.setString(3, this.txtTenhang.getText());
                pst.setString(4, String.valueOf(this.cbxLoaiSP.getSelectedItem()));
                pst.setString(5, String.valueOf(this.cbxNhaCungCap.getSelectedItem()));
                pst.setInt(6, Integer.parseInt(this.txtSoLuong.getText()));
                pst.setDate(7, new java.sql.Date(jdcNgayNhap.getDate().getTime()));
                pst.setInt(8, Integer.parseInt(txtDonGia.getText()));
                pst.setString(9, this.txtTongTien.getText());
                pst.executeUpdate();
                jlbStatus.setText("Lưu thay đổi thành công!");
                disabled();
                refresh();
                Load(sql);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void consistency() {
        String sqlBill = "SELECT * FROM Bill";
        try {

            PreparedStatement pstBill = conn.prepareStatement(sqlBill);
            ResultSet rsBill = pstBill.executeQuery();

            while (rsBill.next()) {

                try {
                    String sqlTemp = "SELECT * FROM Products WHERE ID ='" + rsBill.getString("Code") + "'";
                    PreparedStatement pstTemp = conn.prepareStatement(sqlTemp);
                    ResultSet rsTemp = pstTemp.executeQuery();

                    if (rsTemp.next()) {

                        String sqlUpdate = "UPDATE Products SET QuantityRemaining=? WHERE ID='" + rsBill.getString("Code").trim() + "'";
                        try {
                            pst = conn.prepareStatement(sqlUpdate);
                            pst.setInt(1, rsTemp.getInt("QuantityRemaining") + rsBill.getInt("Amount"));
                            pst.executeUpdate();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void checkProducts() {
        String sqlCheck = "SELECT QuantityRemaining FROM Products WHERE ID='" + txtMaSP.getText() + "'";
        try {
            pst = conn.prepareCall(sqlCheck);
            rs = pst.executeQuery();
            while (rs.next()) {
                if (rs.getInt("QuantityRemaining") == 0) {
                    jlbStatus.setText("Sản phẩm này chưa có");
                    btnSave.setEnabled(false);
                    txtSoLuong.setEnabled(false);
                } else {
                    jlbStatus.setText("Mặt hàng này còn " + rs.getInt("QuantityRemaining") + " sản phẩm!!");
                    btnSave.setEnabled(true);
                    txtSoLuong.setEnabled(true);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        if (Add == true) {
            if (Check()) {
                setTongTien();

                addPhieuNhap();
            } else {
                jlbStatus.setText("Không thể thêm đơn đặt hàng vì mã đơn đặt hàng bạn nhập đã tồn tại");
            }
        } else if (Change == true) {
            changeOrder();
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try {
            JasperReport report = JasperCompileManager.compileReport("C:\\Users\\D.Thanh Trung\\Documents\\NetBeansProjects\\Quan Ly Cua Hang Mua Ban Thiet Bi Dien Tu\\src\\UserInterFace\\Orders.jrxml");

            JasperPrint print = JasperFillManager.fillReport(report, null, conn);

            JasperViewer.viewReport(print, false);
        } catch (JRException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void txtSoLuongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSoLuongActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSoLuongActionPerformed

    private void btnClassifyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClassifyActionPerformed
        if (this.detail.getUser().toString().toString().equals("Admin")) {
            Data data = new Data(detail);
            this.setVisible(false);
            data.setVisible(true);
        }
    }//GEN-LAST:event_btnClassifyActionPerformed

    private void btnNSXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNSXActionPerformed
        if (this.detail.getUser().toString().toString().equals("Admin")) {
            Data data = new Data(detail);
            this.setVisible(false);
            data.setVisible(true);
        }
    }//GEN-LAST:event_btnNSXActionPerformed

    private String cutChar(String arry) {
        return arry.replaceAll("\\D+", "");
    }

    private double convertedToNumbers(String s) {
        String number = "";
        String[] array = s.replace(",", " ").split("\\s");
        for (String i : array) {
            number = number.concat(i);
        }
        return Double.parseDouble(number);
    }


    private void txtSoLuongKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSoLuongKeyReleased
        // TODO add your handling code here:

    }//GEN-LAST:event_txtSoLuongKeyReleased

    private void txtMaSPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMaSPActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMaSPActionPerformed

    private void jdcNgayNhapComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jdcNgayNhapComponentShown
        // TODO add your handling code here:

    }//GEN-LAST:event_jdcNgayNhapComponentShown

    private void cbxLoaiSPPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_cbxLoaiSPPopupMenuWillBecomeInvisible


    }//GEN-LAST:event_cbxLoaiSPPopupMenuWillBecomeInvisible

    private void cbxNhaCungCapPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_cbxNhaCungCapPopupMenuWillBecomeInvisible

    }//GEN-LAST:event_cbxNhaCungCapPopupMenuWillBecomeInvisible

    private void tblNhapHangMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblNhapHangMouseClicked
        cbxLoaiSP.removeAllItems();
        cbxNhaCungCap.removeAllItems();
        int Click=tblNhapHang.getSelectedRow();
        TableModel model=tblNhapHang.getModel();
        
        txtMaNhap.setText(model.getValueAt(Click,0).toString());
        cbxLoaiSP.addItem(model.getValueAt(Click,3).toString());
        txtMaSP.setText(model.getValueAt(Click,1).toString());
        txtTenhang.setText(model.getValueAt(Click, 2).toString());
        cbxNhaCungCap.addItem(model.getValueAt(Click,4).toString());
        txtSoLuong.setText(model.getValueAt(Click, 5).toString());
       ((JTextField) jdcNgayNhap.getDateEditor().getUiComponent()).setText(model.getValueAt(Click, 6).toString());
        txtDonGia.setText(model.getValueAt(Click, 7).toString());
        txtTongTien.setText(model.getValueAt(Click, 8).toString());
        btnChange.setEnabled(true);
        btnDelete.setEnabled(true);
    }//GEN-LAST:event_tblNhapHangMouseClicked

    private void btnBackHomeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBackHomeMouseClicked
        if(this.detail.getUser().toString().toString().equals("Admin")){
            Home home=new Home(detail);
            this.setVisible(false);
            home.setVisible(true);
        }
        else{
            HomeUser home=new HomeUser(detail);
            this.setVisible(false);
            home.setVisible(true);
        }
    }//GEN-LAST:event_btnBackHomeMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(NhapHang.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(NhapHang.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(NhapHang.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NhapHang.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Detail detail = new Detail();
                new NhapHang(detail).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnBackHome;
    private javax.swing.JButton btnChange;
    private javax.swing.JButton btnClassify;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnNSX;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox<String> cbxLoaiSP;
    private javax.swing.JComboBox<String> cbxNhaCungCap;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private com.toedter.calendar.JDateChooser jdcNgayNhap;
    private javax.swing.JLabel jlbStatus;
    private javax.swing.JPanel jpnThongTin;
    private javax.swing.JTable tblNhapHang;
    private javax.swing.JTextField txtDonGia;
    private javax.swing.JTextField txtMaNhap;
    private javax.swing.JTextField txtMaSP;
    private javax.swing.JTextField txtSoLuong;
    private javax.swing.JTextField txtTenhang;
    private javax.swing.JTextField txtTongTien;
    // End of variables declaration//GEN-END:variables
}
