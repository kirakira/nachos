#define SIZE 1024 * 4
int array[SIZE];

int main() {
	int i = 0;
	for(i = 0;i < SIZE;++i)
		array[(i * 2543) % SIZE] = i;
	return 0;
}