package com.intellij.util.lang;

import org.jetbrains.annotations.NonNls;
import sun.misc.Resource;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

public class UrlClassLoader extends ClassLoader {
  private final ClassPath myClassPath;
  @NonNls private static final String CLASS_EXTENSION = ".class";

  public UrlClassLoader(List<URL> urls, ClassLoader parent) {
    super(parent);

    myClassPath = new ClassPath(urls.toArray(new URL[urls.size()]));
  }

  protected Class findClass(final String name) throws ClassNotFoundException {
    Resource res = myClassPath.getResource(name.replace('.', '/').concat(CLASS_EXTENSION), false);
    if (res == null) {
      throw new ClassNotFoundException(name);
    }

    try {
      return defineClass(name, res);
    }
    catch (IOException e) {
      throw new ClassNotFoundException(name, e);
    }
  }

  private Class defineClass(String name, Resource res) throws IOException {
    byte[] b = res.getBytes();
    return _defineClass(name, b);
  }

  protected Class _defineClass(final String name, final byte[] b) {
    return defineClass(name, b, 0, b.length);
  }

  public URL findResource(final String name) {
    String n = name;

    if (n.startsWith("/")) n = n.substring(1);
    Resource res = myClassPath.getResource(n, true);
    if (res == null) return null;
    return res.getURL();
  }

  protected Enumeration<URL> findResources(String name) throws IOException {
    return myClassPath.getResources(name, true);
  }
}