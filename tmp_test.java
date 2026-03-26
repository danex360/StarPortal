
import java.util.Base64;

public class tmp_test {
    public static void main(String[] args) {
        String[] skins = {
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmI2M2Q5NTI2OTQyMTI5NTk1NzNmNzkyNmMzY2UwOGU5ZWE0NTUxMzg1ZWZiZjg3ZTgzYzgyYjYzOTY3YTIxNCJ9fX0=", // 0
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzg2YjkyOTc0OTI3NjFhZGIzOWUxNDc5YTcyMWNmMjU5ZmIyZjVjNWUwMWI0ZDQ5NDllYWIxYTMyNzJjOWIyZCJ9fX0=", // 1
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWE3ZDNlZWMyNGMyMzk3MGQ2YzA2YmY2MTg0MGMyYTY2ZTNmMzQ3YmU3NmQyOWYxMjY1YWVmYTY4ZTFhZjkyMyJ9fX0=", // 2
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTc4ZmMyNDg2ODViMTQzMWNkMzY1YjVjZDkxZGE3OTgzYjYwNWVjNGE4NjQxZTA2MzgzNDhhOTAyYjUyNDljZCJ9fX0=", // 3
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTRmNTJkZmRjYjk5YjFhMTQ5YmMyMzcwOWExYTczNzc3MjVmYTU1M2Q0NTEyODdjYWY4YjdiZjhhNzY3ODcwMyJ9fX0=", // 4
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGZkMTQyZDYzNzFlZmU5ZmU3YmZjYzQzYTgzYjg3YzFhZTZlZjY0YTMwYTRiY2U5NDYyNDkxMDVlYjFlYTIxMiJ9fX0=", // 5
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDQzYmEwOWYxMmFlMWRlZTUyY2FlYjlkMmZjYjhhNzE2ZTUxMDc3OWYzOWZkYjFkOGUyMTY0ZGNjYjVmZDU4NCJ9fX0=", // 6
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lNjdmZDQxOTI2ODRmMGZjMTgzYmZlZTU0OTQyZjQ4OWYxMzIzZmRjYzIzNTgwOTg0MzI4NDQ3ZmIzNzYxYjgifX19", // 7
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTdjM2VmOTY0YWE2Y2ZiNTI5ODc4MjU4Y2E5NjY0ZmVhOTkxZDg2NTc3NzhlNGUyNmMzY2NmNzI4YTdlYTAyZSJ9fX0=", // 8
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWM5ODg1MTE1NWIwNjcyOTI2MjU0NDMyYjcxNmE3YmM2MjJhNzdiZDhjNTgzZWY5NjVkZTcxNjMyODRlNDMifX19"  // 9
        };

        for (int i = 0; i < skins.length; i++) {
            try {
                String decoded = new String(Base64.getDecoder().decode(skins[i]));
                System.out.println(i + ": " + decoded);
            } catch (Exception e) {
                System.out.println(i + ": ERROR " + e.getMessage());
            }
        }
    }
}
