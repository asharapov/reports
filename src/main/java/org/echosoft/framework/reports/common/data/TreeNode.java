package org.echosoft.framework.reports.common.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.echosoft.framework.reports.common.utils.StringUtil;

/**
 * @author Anton Sharapov
 */
public class TreeNode<K, T> {
    private final K id;
    private T data;
    private TreeNode<K, T> parent;
    private List<TreeNode<K, T>> children;

    public TreeNode(final K id, final T data) {
        this(id, data, null);
    }

    protected TreeNode(final K id, final T data, final TreeNode<K, T> parent) {
        this.id = id;
        this.data = data;
        this.parent = parent;
    }

    public K getId() {
        return id;
    }

    public T getData() {
        return data;
    }
    public void setData(final T data) {
        this.data = data;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public TreeNode<K, T> getParent() {
        return parent;
    }

    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    public boolean hasChildren() {
        return children != null && children.size() > 0;
    }

    public int getChildrenCount() {
        return children != null ? children.size() : 0;
    }

    public TreeNode<K, T> getChildNode(final int index) {
        if (children == null)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: 0");
        return children.get(index);
    }

    public Iterable<TreeNode<K, T>> getChildren() {
        return children != null ? children : Collections.<TreeNode<K, T>>emptyList();
    }

    public int getLevel() {
        int level = 0;
        for (TreeNode<K, T> p = getParent(); p != null; p = p.getParent()) level++;
        return level;
    }

    public TreeNode<K, T> findNodeById(final K id, final boolean recursive) {
        if (recursive) {
            return id != null
                    ? findNodeByIdRecursive(id)
                    : findNodeByNullIdRecursive();
        } else {
            return id != null
                    ? findNodeById(id)
                    : findNodeByNullId();
        }
    }
    private TreeNode<K, T> findNodeByIdRecursive(final K id) {
        if (id.equals(this.id))
            return this;
        if (children != null) {
            for (int i = 0, cnt = children.size(); i < cnt; i++) {
                final TreeNode<K, T> node = children.get(i);
                final TreeNode<K, T> result = node.findNodeByIdRecursive(id);
                if (result != null)
                    return result;
            }
        }
        return null;
    }
    private TreeNode<K, T> findNodeByNullIdRecursive() {
        if (id == null)
            return this;
        if (children != null) {
            for (int i = 0, cnt = children.size(); i < cnt; i++) {
                final TreeNode<K, T> node = children.get(i);
                final TreeNode<K, T> result = node.findNodeByNullIdRecursive();
                if (result != null)
                    return result;
            }
        }
        return null;
    }
    private TreeNode<K, T> findNodeByNullId() {
        if (id == null)
            return this;
        if (children != null) {
            for (int i = 0, cnt = children.size(); i < cnt; i++) {
                final TreeNode<K, T> node = children.get(i);
                if (node.id == null)
                    return node;
            }
        }
        return null;
    }
    private TreeNode<K, T> findNodeById(final K id) {
        if (id.equals(this.id))
            return this;
        if (children != null) {
            for (int i = 0, cnt = children.size(); i < cnt; i++) {
                final TreeNode<K, T> node = children.get(i);
                if (id.equals(node.id))
                    return node;
            }
        }
        return null;
    }

    public TreeNode<K, T> addChildNode(final K id, final T data) {
        final TreeNode<K, T> node = new TreeNode<K, T>(id, data, this);
        if (children == null)
            children = new ArrayList<>();
        children.add(node);
        return node;
    }


    public void remove() {
        if (parent != null) {
            parent.children.remove(this);
            parent = null;
        }
    }

    public String debugInfo() {
        try {
            final StringBuilder out = new StringBuilder(512);
            debugInfo(out, 0);
            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    public void debugInfo(final Appendable out) throws IOException {
        debugInfo(out, 0);
    }
    private void debugInfo(final Appendable out, final int level) throws IOException {
        final String prefix = StringUtil.leadLeft("", ' ', level * 3);
        out.append(prefix);
        out.append("Node{id:");
        out.append(String.valueOf(id));
        out.append(", data:");
        out.append(String.valueOf(data));
        out.append("}\n");
        for (TreeNode<K, T> node : getChildren()) {
            node.debugInfo(out, level + 1);
        }
    }

    public Iterable<TreeNode<K, T>> traverseNodes(final boolean includeRoot) {
        return new Iterable<TreeNode<K, T>>() {
            @Override
            public Iterator<TreeNode<K, T>> iterator() {
                return new Walker<>(TreeNode.this, includeRoot);
            }
        };
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(32);
        buf.append("[TreeNode{id:").append(id).append(", parent:").append(parent != null ? parent.id : "null");
        buf.append(", data:").append(data).append("}]");
        return buf.toString();
    }


    public static class Walker<K, T> implements Iterator<TreeNode<K, T>> {
        private final ArrayList<Iterator<TreeNode<K, T>>> stack;
        private TreeNode<K, T> current;
        private TreeNode<K, T> next;
        private boolean nextCalculated;
        public Walker(final TreeNode<K, T> root, final boolean includeRoot) {
            stack = new ArrayList<>();
            if (includeRoot) {
                stack.add(Collections.singletonList(root).iterator());
            } else {
                stack.add(root.getChildren().iterator());
            }
        }

        @Override
        public boolean hasNext() {
            if (!nextCalculated)
                calculateNext();
            return next != null;
        }

        @Override
        public TreeNode<K, T> next() {
            if (!nextCalculated)
                calculateNext();
            if (next == null)
                throw new NoSuchElementException();
            this.current = next;
            this.next = null;
            this.nextCalculated = false;
            return current;
        }

        @Override
        public void remove() {
            if (current == null || stack.size() <= 1)
                throw new IllegalStateException();

            if (current.hasChildren()) {
                stack.remove(stack.size() - 1);
            }
            final Iterator<TreeNode<K, T>> iter = stack.get(stack.size() - 1);
            iter.remove();
            current = null;
            calculateNext();
        }

        protected void calculateNext() {
            nextCalculated = true;
            while (stack.size() > 0) {
                final int index = stack.size() - 1;
                final Iterator<TreeNode<K, T>> iter = stack.get(index);
                if (iter.hasNext()) {
                    next = iter.next();
                    if (next.hasChildren())
                        stack.add(next.getChildren().iterator());
                    return;
                } else {
                    stack.remove(index);
                }
            }
            next = null;
        }
    }
}
