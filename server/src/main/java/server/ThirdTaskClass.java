package server;

public class ThirdTaskClass {
    private static int[] getArrayAfterLast1Or4Method(int[] arr){
        Integer currentFourOrOne = consistFourOrOne(arr);
        if(currentFourOrOne == null){
            return null;
        }
        else{
            int[] array = new int[arr.length - currentFourOrOne - 1];
            System.arraycopy(arr, currentFourOrOne + 1, array, 0, arr.length - currentFourOrOne - 1);
            return array;
        }

    }

    private static Integer consistFourOrOne(int[] arr){
        Integer x = 0;
        for(int i : arr){
            if(i == 4 || i == 1) return x;
            x++;
        }
        try {
            throw new RuntimeException("\nВ массиве нет 1 и 4");
        } catch(RuntimeException e){
            e.printStackTrace();
            return null;
        }
    }

    private static void toPrint(int[] ar){
        for(int i : ar){
            System.out.print(i + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        int[][] arrayz = new int[][]{{1, 2, 3, 4, 5}, {3, 7, 5, 4 ,3, 4}, {0, -3, 4, 5, 8}};

        for(int[] ar : arrayz){
            System.out.println("Массив перед методом : \n");
            toPrint(ar);
            int[] tech = getArrayAfterLast1Or4Method(ar);
            if(tech != null){
                System.out.println("\nМассив после метода : \n");
                toPrint(tech);
            }

        }
    }
}
