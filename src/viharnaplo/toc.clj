;; ## Viharnaplo
;;
;; "Viharnaplo" is a proof-of-concept demo, and an accompanying
;; presentation.  The main idea is to use Storm to process logs,
;; generate some very simple statistics, and present the results in a
;; browser, dynamically and continously updated.
;;
;; To use it, one will need [leiningen][1], [redis][2] and various
;; other dependencies fetched by lein. See below for installation and
;; setup instructions!
;;
;; # Installation
;;
;; First install [leiningen][1], [redis][2] and [ditaa][3] (see their
;; webpage for instructions, or use the packages from your
;; distribution of choice), then follow these steps:
;;
;; <pre><code>$ git clone git://github.com/algernon/viharnaplo.git
;; $ cd viharnaplo
;; $ git submodule update --init
;; $ lein deps
;; $ lein compile
;; $ ditaa resources/public/flow.txt resources/public/flow.png
;; </code></pre>
;;
;; We will also need to download [flot][4], and extract it to
;; <code>resources/public/flot/</code>.
;;
;; # Running "Viharnaplo"
;;
;; There are two applications to run: the web service, that will serve
;; both the presentation files and the dynamic data as read from
;; Redis.
;;
;; ### Starting the web service
;;
;; <pre><code>$ lein ring server-headless</code></pre>
;;
;; One can use <code>server</code> instead, and then it will open the
;; index page in one's default browser.
;;
;; The presentation (in hungarian) will be ready and available once
;; the web service started up. One does not need to start the Storm
;; app first.
;;
;; ### Starting the Storm app
;;
;; Starting the storm app is easy aswell:
;;
;; <pre><code>$ lein run -m viharnaplo</code></pre>
;;
;; # Troubleshooting
;;
;; In case we run out of file descriptors, or any of the two parts
;; fail with some kind of network error, then there are two knobs to
;; turn on Linux (as root):
;;
;; <pre><code># echo 1 >/proc/sys/net/ipv4/tcp_tw_reuse
;; # echo 1 >/proc/sys/net/ipv4/tcp_tw_recycle</code></pre>
;;
;; [1]: https://github.com/technomancy/leiningen
;; [2]: http://redis.io/
;; [3]: http://ditaa.org/
;; [4]: http://flot.googlecode.com/files/flot-0.7.tar.gz

(ns viharnaplo.toc)
