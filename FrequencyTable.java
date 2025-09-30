import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

class FrequencyTable implements java.io.Serializable {

    private class Node { // entry class
        Object key;
        Node next;
        int count;
        Object value;

        // Object value;
        Node(Object k, Node n) {
            this.key = k;
            this.next = n;
            this.count = 1; // set the count to 1
        }

        Node(Object k,Object v,Node n) {
            this.key = k;
            this.next = n;
            this.value = v;
            this.count = 1;
        }
    }

    Node[] table = new Node[8]; // always a power of 2
    int size = 0;

    boolean contains(Object key) { //
        int h = key.hashCode();
        int i = h & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.equals(e.key))
                return true;
        }
        return false;
    }

    void add(Object key) {
        int h = key.hashCode(); // random number
        int i = h & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.equals(e.key)) {
                e.count++;
                return;
            }
        }
        table[i] = new Node(key, table[i]); // if the index is null, hence count = 1
        size++;
        if ((float)size/table.length >= 0.75f)
            resize();
    }

    void putTFIDF(Object key, double value) {
        int h = key.hashCode(); // random number
        int i = h & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.equals(e.key)) {
                e.value = value;
                return;
            }
        }
        table[i] = new Node(key,value,table[i]); // if the index is null, hence count = 1
        size++;
        if ((float)size/table.length >= 0.75f)
            resize();

    }

    double getTFIDF(Object key) {
        int h = key.hashCode();
        int i = h & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.equals(e.key) && e.value != null) {
                return (Double) e.value;
            }
        }
        return 0.0;  // Return 0 if not found
    }

    void resize() {
        Node[] oldTable = table;
        int oldCapacity = oldTable.length;
        int newCapacity = oldCapacity << 1; // eqvalent to * 2
        Node[] newTable = new Node[newCapacity];
        for (int i = 0; i < oldCapacity; ++i) {
            for (Node e = oldTable[i]; e != null; e = e.next) {
                int h = e.key.hashCode();
                int j = h & (newTable.length - 1);
                newTable[j] = new Node(e.key, newTable[j]);
            }
        }
        table = newTable;
    }

    void printAll() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < table.length; ++i)
            for (Node e = table[i]; e != null; e = e.next)
                stringBuilder.append(e.key + ":" + e.count + ",");
                System.out.println(stringBuilder);
    }


    public List<String> getAllWords(){
        List<String> words = new ArrayList<>();
        for(int i = 0; i < table.length; ++i){
            for(Node e = table[i]; e != null; e = e.next){
                words.add(e.key.toString());
            }
        }
        return words;
    }

    public int getTotalWordsCount(){
        int t = 0;
        for(int i = 0; i < table.length; ++i){
            for(Node e = table[i]; e != null; e = e.next){
                t += e.count;
            }
        }
        return t;
    }

    public int getWordCount(String word) {
        int h = word.hashCode();
        int i = h & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (word.equals(e.key)) {
                return e.count;
            }
        }
        return 0;
    }

    public int getCount(String word){
        return getWordCount(word);
    }

    private void writeObject(ObjectOutputStream s) throws Exception {
        s.defaultWriteObject();
        s.writeInt(size);
        for (int i = 0; i < table.length; ++i) {
            for (Node e = table[i]; e != null; e = e.next) {
                s.writeObject(e.key);
            }
        }
    }
    private void readObject(ObjectInputStream s) throws Exception {
        s.defaultReadObject();
        int n = s.readInt();
        for (int i = 0; i < n; ++i)
            add(s.readObject());
    }
}
