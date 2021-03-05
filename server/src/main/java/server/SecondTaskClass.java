package server;

public class SecondTaskClass {

    private static int[] getArrayAfterLast4Method(int[] arr){
        Integer currentFour = consistFour(arr);
        if(currentFour == null){
            return null;
        }
        else{
            int[] array = new int[arr.length - currentFour - 1];
            System.arraycopy(arr, currentFour + 1, array, 0, arr.length - currentFour - 1);
            return array;
        }

    }

    private static Integer consistFour(int[] arr){
        Integer x = 0;
        for(int i : arr){
            if(i == 4) return x;
            x++;
        }
        try {
            throw new RuntimeException("\nВ массиве нет 4");
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
            int[] tech = getArrayAfterLast4Method(ar);
            if(tech != null){
                System.out.println("\nМассив после метода : \n");
                toPrint(tech);
            }

        }
    }

}
