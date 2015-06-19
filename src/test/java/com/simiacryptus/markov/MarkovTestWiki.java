package com.simiacryptus.markov;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.simiacryptus.markov.MarkovCoder;
import com.simiacryptus.markov.MarkovCoder2;

public class MarkovTestWiki extends MarkovTest
{
  
  static final boolean SERIALIZATION_CHECKS = false;
  
  public static int extractPages(final File root, final int pageLimit)
      throws FileNotFoundException, IOException
  {
    // From http://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles-multistream.xml.bz2
    final AtomicInteger hitPageLimit = new AtomicInteger(-1);
    final InputStream in = new BZip2CompressorInputStream(new FileInputStream(
        "..\\enwiki-latest-pages-articles-multistream.xml.bz2"), true);
    try
    {
      final SAXParserFactory spf = SAXParserFactory.newInstance();
      spf.setNamespaceAware(false);
      final SAXParser saxParser = spf.newSAXParser();
      saxParser.parse(in, new DefaultHandler() {
        Stack<String>                     prefix     = new Stack<String>();
        Stack<Map<String, AtomicInteger>> indexes    = new Stack<Map<String, AtomicInteger>>();
        private String                    title;
        private int                       pages      = 0;
        private boolean                   verbose    = false;
        
        StringBuilder                     nodeString = new StringBuilder();
        
        @Override
        public void characters(final char[] ch, final int start,
            final int length) throws SAXException
        {
          this.nodeString.append(ch, start, length);
          super.characters(ch, start, length);
        }
        
        @Override
        public void endDocument() throws SAXException
        {
          super.endDocument();
        }
        
        @Override
        public void endElement(final String uri, final String localName,
            final String qName) throws SAXException
        {
          final String pop = this.prefix.pop();
          this.indexes.pop();
          
          final int length = this.nodeString.length();
          String text = this.nodeString.toString().trim();
          this.nodeString = new StringBuilder();
          
          if ("page".equals(qName))
          {
            this.title = null;
          }
          else if ("title".equals(qName))
          {
            this.title = text;
          }
          else if ("text".equals(qName))
          {
            final File file = new File(root, this.title + ".txt");
            try
            {
              file.getParentFile().getCanonicalFile().mkdirs();
              final FileWriter out = new FileWriter(file);
              out.write(text);
              out.close();
            }
            catch (final IOException e)
            {
              throw new RuntimeException(e);
            }
            if (this.pages++ > pageLimit)
            {
              hitPageLimit.set(this.pages);
              throw new RuntimeException("Page limit reached");
            }
          }
          
          if (this.verbose)
          {
            text = this.nodeString.toString().trim().replaceAll("\n", "\\\\n")
                .replaceAll("\r", "\\\\r");
            final int maxLength = 120;
            if (text.length() > maxLength)
            {
              text = text.substring(0, maxLength) + "...";
            }
            System.out.println(String.format("%s (%s bytes): %s", pop, length,
                text));
          }
          super.endElement(uri, localName, qName);
        }
        
        @Override
        public void startDocument() throws SAXException
        {
          super.startDocument();
        }
        
        @Override
        public void startElement(final String uri, final String localName,
            final String qName, final Attributes attributes)
            throws SAXException
        {
          int idx;
          if (0 < this.indexes.size())
          {
            final Map<String, AtomicInteger> index = this.indexes.peek();
            AtomicInteger cnt = index.get(qName);
            if (null == cnt)
            {
              cnt = new AtomicInteger(-1);
              index.put(qName, cnt);
            }
            idx = cnt.incrementAndGet();
          }
          else
          {
            idx = 0;
          }
          String path = 0 == this.prefix.size() ? qName : this.prefix.peek()
              + "/" + qName;
          if (0 < idx)
          {
            path += "[" + idx + "]";
          }
          this.prefix.push(path);
          this.indexes.push(new HashMap<String, AtomicInteger>());
          super.startElement(uri, localName, qName, attributes);
        }
        
      }, null);
    }
    catch (final Exception e)
    {
      final int i = hitPageLimit.get();
      if (-1 < i)
      {
        return i;
      }
      else
      {
        throw new RuntimeException(e);
      }
    }
    finally
    {
      in.close();
    }
    return hitPageLimit.get();
  }
  
  public MarkovTestWiki() throws FileNotFoundException, IOException
  {
    super();
    ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(false);
    this.root = new File("..\\enwiki-temp");
    this.root.mkdirs();
    extractPages(this.root, 1000);
  }
  
  @Override
  public MarkovCoder getCoder()
  {
    return new MarkovCoder2(5);
  }
  
  @Override
  @Test
  public void sourceCompressIncrementalDictionary() throws Exception
  {
    super.sourceCompressIncrementalDictionary();
  }
  
  @Override
  @Test
  public void sourceCompressIndividuals() throws Exception
  {
    super.sourceCompressIndividuals();
  }
  
  @Override
  @Test
  public void sourceCompressSharedDictionary() throws Exception
  {
    super.sourceCompressSharedDictionary();
  }
  
}