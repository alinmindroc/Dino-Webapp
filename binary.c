#include<stdio.h>

void func1(int *x){
	*x = 0;
}

int func2(int x){
	return x * 2;
}

int func3(int x){
	if(x == 0)
		return 0;
	else
		return func2(x);
}

int funcrec(int x){
	if(x < 0)
		return 0;
	return funcrec(x-1);
}

int main(void){
	int x = 1;
	printf("%d ", func3(x));
	func1(&x);
	printf("%d ", func3(x));

	printf("%d", funcrec(10));

	return 0;
}
