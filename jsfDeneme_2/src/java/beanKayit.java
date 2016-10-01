
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.faces.bean.*;
import javax.faces.context.FacesContext;

@ManagedBean(name = "yazarlarBean")
@RequestScoped
public class beanKayit implements Serializable {

    public beanKayit() {
        KayitCek();//web sayfası ilk açıldığında bütün kayıtlar okunsun ve ekranda gösterilsin
    }
    private static final long serialVersionUID = 1L;
    Connection con;
    List<yazarlar> liste = new ArrayList<yazarlar>();
    PreparedStatement ps;
    int ID;
    private String isim;
    private String alani;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public List<yazarlar> getListe() {
        return liste;
    }

    public void setListe(List<yazarlar> liste) {
        this.liste = liste;
    }

    public String getIsim() {
        return isim;
    }

    public void setIsim(String isim) {
        this.isim = isim;
    }

    public String getAlani() {
        return alani;
    }

    public void setAlani(String alani) {
        this.alani = alani;
    }

    public String KayitCek() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3307/firmaveritabani", "root", "root");
            PreparedStatement ps = con.prepareStatement("SELECT DISTINCT adi,alani,ID FROM yazarlar");//İsme göre sorgu yapıyoruz.
            //ps.setString(1,"ID");//İsmin tamamını değil bir kısmını girseniz bile o kısmı içeren isimleri sorguya koyar.
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                yazarlar yz = new yazarlar();
                yz.setID(rs.getInt("ID"));//Güncelleme için eşsiz alan lazım.ID bizim eşsiz alanımız.
                yz.setAdi(rs.getString("adi"));
                yz.setAlani(rs.getString("alani"));
                yz.setGuncellenebilirlik(false);//İlk başta normal yazı halinde gelmesi için güncellenebilirlik kapalı.inputText lerin görünmemesi için.
                liste.add(yz);

            }
        } catch (Exception e) {
            System.err.println(e);
        }

        return null;
    }

//Güncelle linkine tıklayınca bu metot çalışacak.
    public String degistirileniKaydet() {
        for (yazarlar yzListe : liste) {
            yzListe.setGuncellenebilirlik(false);
            if (yzListe.guncellenebilirlik == false) {
                guncelle();
            }
        }

        return null;
    }

    //Düzenle linkine tıklayınca bu metot çalışacak.
    public String guncellenebilirligiDegistir(yazarlar yz) {
        yz.setGuncellenebilirlik(true);
        //Kayıt güncellemek için güncellenebilirliği tekrar açılır. Varsayılan olarak kapalı. 
        return null;
    }

    public String KayitSil() {

        int i = 0;
        FacesContext fc = FacesContext.getCurrentInstance();
        this.ID = Integer.parseInt(IDParametresiniAl(fc));//<f:param name="id" value="#{p.id}"/>//param etiketini name ine dikkat et

        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3307/firmaveritabani", "root", "root");
            ps = con.prepareStatement("DELETE FROM yazarlar WHERE ID=?");
            ps.setInt(1, ID);//<f:param name="id" value="#{p.id}"/>//param etiketini name ine dikkat et
            i = ps.executeUpdate();
            con.close();
            ps.close();

        } catch (Exception e) {
            System.out.print(e);
        }
        while (true) {
            if (i > 0) {
                liste.remove(i);//silinen kaydı listeden çıkar
                
                

                return "basarili";
            } else {
                return "silmebasarisiz";
            }
        }
    }

//Silinecek kaydın ID bilgisini tutacak metodumuz
    public String IDParametresiniAl(FacesContext fc) {
        Map<String, String> parametreler = fc.getExternalContext().getRequestParameterMap();
        return parametreler.get("ID");

    }

    public String verileriKaydet()//Sayfadan girilen verileri veri tabanına gönderem metot.
    {
        int i = 0;
        PreparedStatement ps = null;//Veri tabanına gönderilecek bilgileri bu nesne tuttacak ve veri tabanına gönderecek.
        Connection con = null;//Veri tabanına bağlantı yapmamızı sağlayacak nesne.
        try {
            Class.forName("com.mysql.jdbc.Driver");//Hangi türde bir veri tabanını kullanacağını bildiriyoruz.
            con = DriverManager.getConnection("jdbc:mysql://localhost:3307/firmaveritabani", "root", "root");//Bağlanacağı veri tabanını ve kullanacağı kullanıcı adı-parolayı bildiriyoruz.
            //String sql = "INSERT INTO yazarlar(Adı,Alanı) VALUES(?,?)";//Yazarlar tablosunun Adı ve Alanı sütununa değer göndereceğimi söylüyoruz.
            ps = con.prepareStatement("INSERT INTO yazarlar(adi, alani) VALUES(?,?)");//ps nesnesine SQL komutunu bildiriyoruz.İsterseniz parametre olarak SQL kodu yerine üstteki sql de verebilirsiniz.
            ps.setString(1, isim);//ps nesnesine gelen ismi koyduk.
            ps.setString(2, alani);//ps nesnesine gelen alanı koyduk.
            i = ps.executeUpdate();//executeUpdate verilen sorguyu çalıştırır ve integer değer döndürür.
            ps.close();
            con.close();
            

            //exequteUdate eğer 0'dan büyük değer döndürürse kayıt başarılı olmuş demektir. 
        } catch (Exception e)//Hata olduğunda konsola verilecek.
        {
            System.out.println(e);
        } finally //Ne olursa olsun her koşulda çalışacak kısım 
        {
            try {
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        if (i > 0) //Sorgu başarılı olarak çalışınca i 0'dan büyük oluyor ve bizi başarılı sayfasına yönlediriyor.
        {
            alani = "";//kayıt eklendi inputTexti boşalt
            isim = "";//kayıt eklendi inputTexti boşalt
//            KayitCek();// Güncel verileri tekrar oku
            return "basarili";

        } else //Sorgu başarısız ise başarısız sayfasına gidiyoruz.
        {
            return "basarisiz";

        }
    }

    public boolean guncelle() {
        {
            int i = 0;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection("jdbc:mysql://localhost:3307/firmaveritabani", "root", "root");
                PreparedStatement ps = con.prepareStatement("UPDATE yazarlar SET adi=?,alani=? WHERE ID=?");
                for (Iterator<yazarlar> it = liste.iterator(); it.hasNext();) {
                    yazarlar item = it.next();
                    ps.setString(1, item.adi);
                    ps.setString(2, item.alani);
                    ps.setInt(3, item.ID);
                    i = ps.executeUpdate();
                }
            } catch (Exception e) {
                System.err.print(e);
            }
            if (i > 0) {
//                KayitCek();

                return true;//İşlemin başarılı olması durumunda true döner.Kayıt Güncellenmiş demektir.
            } else {
                return false;//Başarısız olma durumunda false döner.Kayıt Güncellenemedi demektir.
            }
        }
    }
}
