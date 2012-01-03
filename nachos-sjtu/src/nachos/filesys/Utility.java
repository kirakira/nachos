package nachos.filesys;

public class Utility {
    public static void stringToBytes(String s, int limit, byte[] buffer, int offset) {
        Lib.assertTrue(s.length() <= limit, "String too long");

        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            buffer[offset + i * 2] = (char) (c & 0xff);
            buffer[offset + i * 2 + 1] = (char) ((c >> 8) & 0xff);
        }
        buffer[offset + s.length() * 2] = 0;
        buffer[offset + s.length() * 2 + 1] = 0;
    }

    public static String bytesToString(int limit, byte[] buffer, int offset) {
        StringBuilder s;
        for (int i = 0; i < limit; ++i) {
            char c = ((((char) buffer[offset + i * 2]) & 0xff) << 0) | ((((char) buffer[offset + i * 2 + 1]) & 0xff) << 8);
            if (c == 0)
                break;
            s.append(c);
        }
        return s.toString();
    }
}
