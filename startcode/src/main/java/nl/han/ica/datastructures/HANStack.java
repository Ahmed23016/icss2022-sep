package nl.han.ica.datastructures;

public class HANStack<T> implements IHANStack<T> {

    private Node<T> stackhead;

    public HANStack() {
        stackhead = null;
    }

    @Override
    public void push(T value) {
        stackhead = new Node<>(value, stackhead);
    }

    @Override
    public T pop() {
        if (stackhead == null){
            return null;
            }

        T value = stackhead.value;
        stackhead = stackhead.next;
        return value;
    }

    @Override
    public T peek() {
        return stackhead == null? null:stackhead.value;
    }

    public boolean isEmpty() {
        return null == stackhead;
    }

    public int size() {
        int countOfStack = 0;
        Node<T> currentItem = stackhead;
        while (currentItem != null) {
            countOfStack++;
            currentItem = currentItem.next;
        }
        return countOfStack;
    }
}
