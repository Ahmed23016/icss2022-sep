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


}
