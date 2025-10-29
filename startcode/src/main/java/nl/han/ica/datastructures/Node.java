package nl.han.ica.datastructures;

class Node<T> {
    T value;
    Node<T> next;

    Node(T value, Node<T> next) {
        this.value = value;
        this.next = next;
    }
}
