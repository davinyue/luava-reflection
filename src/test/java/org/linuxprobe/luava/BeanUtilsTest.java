package org.linuxprobe.luava;

import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.linuxprobe.luava.reflection.BeanUtils;
import org.linuxprobe.luava.reflection.BeanUtils.CopyOption;

import java.util.ArrayList;
import java.util.List;

public class BeanUtilsTest {

    private static ItemA itemA = new ItemA();
    private static ItemB itemB = new ItemB();

    static {
        itemA.setCode("itemA");
        itemA.setSex(true);
        ArrayList<SubItem> aSubItems = new ArrayList<>();
        SubItem a = new SubItem();
        a.setName("a");
        aSubItems.add(a);
        itemA.setSubItem(a);
        itemA.setSubItems(aSubItems);


        itemB.setNumber("itemA");
        itemB.setScore(1);
        ArrayList<SubItem> bSubItems = new ArrayList<>();
        SubItem b = new SubItem();
        b.setName("b");
        bSubItems.add(b);
        itemB.setItem(b);
        itemB.setItems(bSubItems);
    }



    @Test
    public void copyFieldsTest() {
        ItemA targetA = new ItemA();
        BeanUtils.copyProperties(itemA, targetA);
        Assert.assertEquals(itemA.getCode(), targetA.getCode());
        Assert.assertEquals(itemA.isSex(), targetA.isSex());
        Assert.assertEquals(itemA.getSubItem().getName(), targetA.getSubItem().getName());
        Assert.assertEquals(itemA.getSubItems().size(), targetA.getSubItems().size());
        Assert.assertEquals(itemA.getSubItems().get(0).getName(), targetA.getSubItems().get(0).getName());

        targetA = new ItemA();
        BeanUtils.copyProperties(itemA, targetA, "subItem", "subItems");
        Assert.assertEquals(itemA.getCode(), targetA.getCode());
        Assert.assertEquals(itemA.isSex(), targetA.isSex());
        Assert.assertNull(targetA.getSubItem());
        Assert.assertNull(targetA.getSubItems());
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void copyFieldsWithOption() throws IllegalArgumentException {

        // 断言 IllegalArgumentException
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Can not copy the value of the field named 'sex' to the field 'score'.");

        BeanUtils.CopyOption option = new BeanUtils.CopyOption();
        option.addIgnoreFields("code");
        ItemA targetA = new ItemA();
        BeanUtils.copyProperties(itemA, targetA, option);
//        Assert.assertEquals(itemA.getCode(), targetA.getCode());
        Assert.assertNull(targetA.getCode());
        Assert.assertEquals(itemA.isSex(), targetA.isSex());
        Assert.assertEquals(itemA.getSubItem().getName(), targetA.getSubItem().getName());
        Assert.assertEquals(itemA.getSubItems().size(), targetA.getSubItems().size());
        Assert.assertEquals(itemA.getSubItems().get(0).getName(), targetA.getSubItems().get(0).getName());

        option.removeIgnoreFields("code");
        option.addIgnoreFields("sex");
        option.addFieldMapping("code", "number");
        option.addFieldMapping("subItem", "item");
        option.addFieldMapping("subItems", "items");
        ItemB targetB = new ItemB();
        BeanUtils.copyProperties(itemA, targetB, option);
        Assert.assertEquals(itemA.getCode(), targetB.getNumber());
        Assert.assertEquals(targetB.getScore(), 0);
        Assert.assertEquals(itemA.getSubItem().getName(), targetB.getItem().getName());
        Assert.assertEquals(itemA.getSubItems().size(), targetB.getItems().size());
        Assert.assertEquals(itemA.getSubItems().get(0).getName(), targetB.getItems().get(0).getName());


        option.removeIgnoreFields("sex");
        option.addFieldMapping("sex", "score");
        targetB = new ItemB();
        // 引发 IllegalArgumentException
        BeanUtils.copyProperties(itemA, targetB, option);

        option.addFieldMapping("code", "number");
        option.addFieldMapping("subItem", "item");
        option.addFieldMapping("subItems", "items");
        option.setIgnoreError(true);
        targetB = new ItemB();
        BeanUtils.copyProperties(itemA, targetB, option);
        Assert.assertEquals(itemA.getCode(), targetB.getNumber());
        Assert.assertEquals(targetB.getScore(), 0);
        Assert.assertEquals(itemA.getSubItem().getName(), targetB.getItem().getName());
        Assert.assertEquals(itemA.getSubItems().size(), targetB.getItems().size());
        Assert.assertEquals(itemA.getSubItems().get(0).getName(), targetB.getItems().get(0).getName());
    }


    @Test
    public void test() {
        try {
            StudentA studentA = new StudentA();
            StudentB studentB = new StudentB();
            BeanUtils.copyProperties(studentA, studentB, new BeanUtils.CopyOption().addFieldMapping("height", "heIght"));
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


@Getter
@Setter
class ItemA implements Cloneable {
    private String code;

    private boolean sex;

    private SubItem subItem;

    private List<SubItem> subItems;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

@Getter
@Setter
class ItemB {
    private String number;

    private int score;

    private SubItem item;

    private List<SubItem> items;
}


@Setter
@Getter
class SubItem {
    private String name;
}
