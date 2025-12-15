# 📚 Kütüphane Yönetim Sistemi (Library Management System)

Bu proje, **PRJ-1** ödevi kapsamında **Java (Swing)** ve **PostgreSQL** kullanılarak geliştirilmiş kapsamlı bir Kütüphane Otomasyon Sistemidir. Proje, Nesne Yönelimli Programlama (OOP) prensiplerine sadık kalınarak ve **Tasarım Desenleri (Design Patterns)** etkin bir şekilde kullanılarak inşa edilmiştir.

## 🚀 Proje Hakkında
Sistem; kütüphane personelinin (Admin) kitap ve üye yönetimini yapabildiği, üyelerin ise kitap arayıp ödünç alabildiği masaüstü tabanlı bir uygulamadır. Veriler PostgreSQL veritabanında tutulmakta olup, geçmiş hareketler (Loglama) kayıt altına alınmaktadır.

## 🛠️ Kullanılan Teknolojiler ve Araçlar
- **Dil:** Java (JDK 17+)
- **Arayüz (GUI):** Java Swing (JFrame, JPanel)
- **Veritabanı:** PostgreSQL
- **IDE:** IntelliJ IDEA / Eclipse
- **Sürüm Kontrol:** Git & GitHub

## 🏗️ Mimari ve Tasarım Desenleri (Design Patterns)
Projede katmanlı mimari kullanılmış ve aşağıdaki tasarım desenleri uygulanmıştır:

1.  **Singleton Pattern:** Veritabanı bağlantısının (`DatabaseConnection`) tek bir nesne üzerinden yönetilmesi ve kaynak tasarrufu için kullanılmıştır.
2.  **DAO (Data Access Object) Pattern:** Veritabanı işlemleri (`BookDAO`, `UserDAO`) iş mantığından ayrılarak soyutlanmıştır.
3.  **Factory Pattern:** Kullanıcı nesnelerinin (`UserFactory`) oluşturulması sırasında, role göre (Member/Admin) doğru nesnenin üretilmesi sağlanmıştır.
4.  **Observer Pattern:** Bir kitap ödünç alınmak istendiğinde, kitabın durumu değiştiğinde ilgili birimlerin veya bekleme listesindeki üyelerin haberdar edilmesi yapısına uygun altyapı kurulmuştur.
5.  **State Pattern:** Kitabın durumları (`AvailableState`, `LoanedState`) nesneleştirilerek yönetilmiş, if-else karmaşası engellenmiştir.

## ✅ Özellikler (Fonksiyonlar)

### 👤 Personel (Admin) Modülü
- **Üye Yönetimi:** Yeni üye kaydı, üye listeleme, üye arama ve detaylı profil inceleme.
- **Kitap Yönetimi:** Yeni kitap ekleme, kitap silme ve stok takibi.
- **İade İşlemleri:** Kitap iade alma, **gecikme faizi hesaplama** ve geçmişe kaydetme.
- **Raporlama:** Üyenin geçmiş kitap hareketlerini (Loglar) görüntüleme.

### 👥 Üye (Member) Modülü
- **Kitap Arama:** Başlık, yazar veya kategoriye göre dinamik arama.
- **Ödünç Alma:** Müsait durumdaki kitapları tek tıkla ödünç alma.
- **Kişisel Takip:** Üzerindeki kitapları, iade tarihlerini ve varsa cezaları görüntüleme.
- **Profil:** İletişim bilgilerini güncelleme.

## 📂 Diyagramlar
Projenin analiz ve tasarım sürecine ait UML diyagramları (Use-Case, Class, Sequence, ER) proje dosyaları içerisindedir.

---
**Geliştirici:** [Fatih Çiçek]
**Ders:** Yazılım Mimarisi ve Tasarımı
