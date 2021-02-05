package Singletons;

import java.util.Random;

public class ColorPalette {


    private static String[] colors = {"#aaf0d1","#ffa474","#bc5d58","#b2ec5d","#17806d","#fcb4d5","#e3256b","#bab86c","#fce883",
            "#cda4de","#8e4585","#1f75fe","#1a4876","#fe4eda","#c8385a","#ff7538"};

    public static String getColor(int index){
        if(index<colors.length){
            return colors[index];
        }else{
            Random random = new Random();
            int nextInt = random.nextInt(0xffffff + 1);
            return String.format("#%06x", nextInt);
        }
    }


}
