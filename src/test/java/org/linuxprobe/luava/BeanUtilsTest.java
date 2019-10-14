package org.linuxprobe.luava;

import lombok.Getter;
import lombok.Setter;
import org.junit.Test;
import org.linuxprobe.luava.reflection.BeanUtils;
import org.linuxprobe.luava.reflection.BeanUtils.CopyOptions;

public class BeanUtilsTest {
    @Test
    public void test() {
        try {
            StudentA studentA = new StudentA();
            StudentB studentB = new StudentB();
            BeanUtils.copyProperties(studentA, studentB, new CopyOptions().addFieldMapping("height", "heIght"));
            System.out.println(studentB);
            System.out.println(BeanUtils.beanToMap(studentA));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

@Getter
@Setter
class StudentA {
    static String test = "testa";
    private Integer height = 78;
}

@Getter
@Setter
class StudentB {
    static String test = "test";
    private int heIght = 165;
}
