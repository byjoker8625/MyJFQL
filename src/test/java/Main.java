import de.byjoker.myjfql.config.GeneralConfig;
import de.byjoker.myjfql.util.Yaml;

import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
        String s = "423142qewds423erwd5f34trwefd4t2wgresd1t34qegfad2312312a4324re";
        System.out.println(s.length());
        System.out.println(hashCode(s));
        Yaml.INSTANCE.write(new GeneralConfig(), new File("config.yml"));

    }

    static int hashCode(String value) {
        int hash = 1;

        if (value.length() > 0) {
            char[] val = value.toCharArray();

            for (int i = 0; i < value.length(); i++) {
                hash = hash + val[i];
            }
        }

        if (hash > 1000000) {
            hash = 100000;
        }

        if (hash < 1) {
            hash = 1;
        }

        return hash;
    }

}
