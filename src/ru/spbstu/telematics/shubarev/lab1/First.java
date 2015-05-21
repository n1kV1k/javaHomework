package ru.spbstu.telematics.shubarev.lab1;

import java.util.Scanner;

public class First {

	public static void main(String args[]) {

		int n = 4;
		int it = 0;
		double nm = 0.0;
		double tmpD = 0.0;
		int tmpI = 0;
		Scanner sc = new Scanner(System.in);

		System.out.println("Введите число:");
		nm = (double) sc.nextDouble();
		System.out
				.println("Введите точность бинарного преобразования дробной части:");
		n = sc.nextInt();
		sc.close();

		tmpI = (int) (nm / 1);
		tmpD = nm % 1;

		while (tmpI > 0) {
			if (tmpI % 2 > 0)
				it++;
			tmpI /= 2;
		}

		while (n > 0) {
			tmpD *= 2;
			if (tmpD >= 1.0) {
				it++;
				tmpD %= 1;
			}
			n--;
		}

		System.out.println("Количество единиц: " + it);
		return;
	}
}
