package com.simiacryptus.markov;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Test;

import com.simiacryptus.markov.MarkovCoder;
import com.simiacryptus.markov.MarkovCoder1;
import com.simiacryptus.markov.MarkovModel;
import com.simiacryptus.util.TestUtil;
import com.simiacryptus.util.TestUtil.Visitor;

public class MarkovTest
{
  
  static final boolean SERIALIZATION_CHECKS = false;
  
  File                 root                 = new File("src");
  
  public MarkovCoder getCoder()
  {
    return new MarkovCoder1(3);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void wikipedia() throws IOException, URISyntaxException, XMLStreamException, FactoryConfigurationError {
    final String wikiInput = "L:/enwiki-latest-pages-articles.xml.bz2";
    
    XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(
        new BZip2CompressorInputStream(new FileInputStream(wikiInput)));
    reader.forEachRemaining(new Consumer<XMLEvent>() {
      Stack<String> path = new Stack<String>();
      String currentTitle = null; 
      String text = ""; 
      @Override
      public void accept(XMLEvent t) {
        if (t.isStartElement()) {
          path.push(t.asStartElement().getName().getLocalPart());
        } else if (t.isEndElement()){
          path.pop();
        }
        if(t.isCharacters()) {
          if("[mediawiki, page, title]".equals(path.toString()))
          {
            if(!text.isEmpty())
            {
              MarkovTest.this.feed(currentTitle, text, MarkovTest.this.getCoder().newModel());
            }            
            text = "";
            currentTitle = t.toString();
          } else if("[mediawiki, page, revision, text]".equals(path.toString()))
          {
            text += t.toString();
          }
        }
      }
    });
  }

  private void feed(String currentTitle, String text, final MarkovModel<Character> dictionary) {
    try
    {
      System.out.println(String.format("Studying %s",
          currentTitle));
      this.getCoder().study(dictionary, text.getBytes());
      System.out.print(String.format("Compressing %s (%s bytes)...",
          currentTitle, text.length()));
      final byte[] byteArray = MarkovTest.this.getCoder().encode(
          dictionary, text.getBytes());
      System.out.print(String.format("%s bytes (%.3f%%)...",
          byteArray.length, 100. * (text.length() - byteArray.length)
              / text.length()));
      MarkovTest.this.getCoder().verify(dictionary, text.getBytes(), byteArray);
      System.out.println(String.format("verified!"));
      
      final byte[] compressedMarkov = MarkovTest.this.getCoder().encode(
          dictionary);
      System.out.println(String.format(
          "Markov chain compressed to %s bytes", compressedMarkov.length));
    }
    catch (final IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void sourceCompressIncrementalDictionary() throws Exception
  {
    final MarkovModel<Character> dictionary = this.getCoder().newModel();
    TestUtil.visitFiles(this.root, new Visitor<File>() {
      @Override
      public void run(final File file)
      {
        try
        {
          System.out.println(String.format("Studying %s",
              file.getCanonicalPath()));
          MarkovTest.this.getCoder().study(dictionary, file);
          System.out.print(String.format("Compressing %s (%s bytes)...",
              file.getCanonicalPath(), file.length()));
          final byte[] byteArray = MarkovTest.this.getCoder().encode(
              dictionary, file);
          System.out.print(String.format("%s bytes (%.3f%%)...",
              byteArray.length, 100. * (file.length() - byteArray.length)
                  / file.length()));
          MarkovTest.this.getCoder().verify(dictionary, file, byteArray);
          System.out.println(String.format("verified!"));
          
          final byte[] compressedMarkov = MarkovTest.this.getCoder().encode(
              dictionary);
          System.out.println(String.format(
              "Markov chain compressed to %s bytes", compressedMarkov.length));
        }
        catch (final IOException e)
        {
          throw new RuntimeException(e);
        }
      }
      
    });
  }
  
  @Test
  public void sourceCompressIndividuals() throws Exception
  {
    TestUtil.visitFiles(this.root, new Visitor<File>() {
      @Override
      public void run(final File file)
      {
        try
        {
          final MarkovModel<Character> dictionary = MarkovTest.this.getCoder().newModel();
          System.out.println(String.format("Studying %s",
              file.getCanonicalPath()));
          MarkovTest.this.getCoder().study(dictionary, file);
          System.out.print(String.format("Compressing %s (%s bytes)...",
              file.getCanonicalPath(), file.length()));
          final byte[] byteArray = MarkovTest.this.getCoder().encode(
              dictionary, file);
          System.out.print(String.format("%s bytes (%.3f%%)...",
              byteArray.length, 100. * (file.length() - byteArray.length)
                  / file.length()));
          
          final byte[] compressedMarkov = MarkovTest.this.getCoder().encode(
              dictionary);
          System.out.println(String.format(
              "Markov chain compressed to %s bytes", compressedMarkov.length));
          final MarkovModel<Character> dictionary2 = MarkovTest.this.getCoder()
              .decodeModel(compressedMarkov);
          
          MarkovTest.this.getCoder().verify(dictionary2, file, byteArray);
          System.out.println(String.format("verified!"));
        }
        catch (final IOException e)
        {
          throw new RuntimeException(e);
        }
      }
    });
  }
  
  @Test
  public void sourceCompressSharedDictionary() throws Exception
  {
    final MarkovModel<Character> dictionary = this.getCoder().newModel();
    TestUtil.visitFiles(this.root, new Visitor<File>() {
      @Override
      public void run(final File file)
      {
        try
        {
          System.out.println(String.format("Studying %s",
              file.getCanonicalPath()));
          MarkovTest.this.getCoder().study(dictionary, file);
        }
        catch (final IOException e)
        {
          throw new RuntimeException(e);
        }
      }
    });
    
    final byte[] compressedMarkov = this.getCoder().encode(dictionary);
    System.out.println(String.format("Markov chain (%s nodes) compressed to %s bytes",
        dictionary.root.getNodeCount(), compressedMarkov.length));
    final MarkovModel<Character> dictionary2 = this.getCoder().decodeModel(
        compressedMarkov);
    
    final AtomicLong totalUncompressed = new AtomicLong();
    final AtomicLong totalCompressed = new AtomicLong();
    
    TestUtil.visitFiles(this.root, new Visitor<File>() {
      @Override
      public void run(final File file)
      {
        try
        {
          System.out.print(String.format("Compressing %s (%s bytes)...",
              file.getCanonicalPath(), file.length()));
          totalUncompressed.addAndGet(file.length());
          final byte[] byteArray = MarkovTest.this.getCoder().encode(
              dictionary, file);
          totalCompressed.addAndGet(byteArray.length);
          System.out.print(String.format("%s bytes (%.3f%%)...",
              byteArray.length, 100. * (file.length() - byteArray.length)
                  / file.length()));
          MarkovTest.this.getCoder().verify(dictionary2, file, byteArray);
          System.out.println(String.format("verified!"));
          
        }
        catch (final IOException e)
        {
          throw new RuntimeException(e);
        }
      }
      
    });
    
    System.out.println(String.format(
        "Data: %s bytes compressed to %s bytes (%.3f%%)",
        totalUncompressed.get(), totalCompressed.get(), 100.
            * (totalUncompressed.get() - totalCompressed.get())
            / totalUncompressed.get()));
    System.out
        .println(String.format(
            "Data+Dict: %s bytes compressed to %s bytes (%.3f%%)",
            totalUncompressed.get(),
            totalCompressed.get() + compressedMarkov.length,
            100.
                * (totalUncompressed.get() - totalCompressed.get() - compressedMarkov.length)
                / totalUncompressed.get()));
  }

  @SuppressWarnings("resource")
  @Test
  public void git() throws IOException, NoHeadException, GitAPIException{
    MarkovModel<Character> compressionModel = this.getCoder().newModel();
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    Collection<String> dedup = new HashSet<String>();
    Repository repository = builder.setGitDir(new File("L:/code/kryo/.git"))
      .readEnvironment() // scan environment GIT_* variables
      .findGitDir() // scan up the file system tree
      .build();
    RevWalk walk = new RevWalk(repository);
    StreamSupport.stream(new Git(repository).log().call().spliterator(), false).forEach(head->{
      try {
        
        RevTree tree = walk.parseCommit(head.getId()).getTree();
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        //treeWalk.setFilter(PathFilter.create("/"));
        while(treeWalk.next())
        {
          ObjectId objectId = treeWalk.getObjectId(0);
          if(treeWalk.getPathString().endsWith(".jar")) continue;
          if(!treeWalk.getPathString().endsWith(".java")) continue;
          if(!dedup.add(objectId.name())) continue;
          ObjectLoader loader;
          try {
            loader = repository.open(objectId);
          } catch (Exception e) {
            e.printStackTrace();
            continue;
          }
          ByteArrayOutputStream buf = new ByteArrayOutputStream();
          loader.copyTo(buf);
          String text = new String(buf.toByteArray(), Charset.forName("UTF-8"));
          feed(head.getId().name() + "/" + treeWalk.getPathString(), text, compressionModel);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }
  
}