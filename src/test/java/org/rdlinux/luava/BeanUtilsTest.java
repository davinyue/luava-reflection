package org.rdlinux.luava;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rdlinux.luava.reflection.BeanUtils;
import org.rdlinux.luava.reflection.CopyOption;

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

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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

    @Test
    public void copyFieldsWithOption() throws IllegalArgumentException {

        // 断言 IllegalArgumentException
        this.expectedException.expect(IllegalArgumentException.class);
        this.expectedException.expectMessage("Can not copy the value of the field named 'sex' to the field 'score'.");

        CopyOption option = new CopyOption();
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
            BeanUtils.copyProperties(studentA, studentB, new CopyOption().addFieldMapping("height", "heIght"));
            System.out.println(studentB);
            System.out.println(BeanUtils.beanToMap(studentA));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class StudentA {
    static String test = "testa";
    private Integer height = 78;

    public static String getTest() {
        return test;
    }

    public static void setTest(String test) {
        StudentA.test = test;
    }

    public Integer getHeight() {
        return this.height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }
}

class StudentB {
    static String test = "test";
    private int heIght = 165;

    public static String getTest() {
        return test;
    }

    public static void setTest(String test) {
        StudentB.test = test;
    }

    public int getHeIght() {
        return this.heIght;
    }

    public void setHeIght(int heIght) {
        this.heIght = heIght;
    }
}


class ItemA implements Cloneable {
    private String code;

    private boolean sex;

    private SubItem subItem;

    private List<SubItem> subItems;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isSex() {
        return this.sex;
    }

    public void setSex(boolean sex) {
        this.sex = sex;
    }

    public SubItem getSubItem() {
        return this.subItem;
    }

    public void setSubItem(SubItem subItem) {
        this.subItem = subItem;
    }

    public List<SubItem> getSubItems() {
        return this.subItems;
    }

    public void setSubItems(List<SubItem> subItems) {
        this.subItems = subItems;
    }
}

class ItemB {
    private String number;

    private int score;

    private SubItem item;

    private List<SubItem> items;

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public SubItem getItem() {
        return this.item;
    }

    public void setItem(SubItem item) {
        this.item = item;
    }

    public List<SubItem> getItems() {
        return this.items;
    }

    public void setItems(List<SubItem> items) {
        this.items = items;
    }
}


class SubItem {
    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
