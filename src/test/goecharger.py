#!/usr/bin/env python
"""
Very simple HTTP server in python.
Usage::
    ./webserver.py [<port>]
"""
from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import SocketServer


class S(BaseHTTPRequestHandler):

    global do_status

    def _set_headers(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    def do_status(self):
        self.wfile.write("{\"version\":\"B\",\"rbc\":\"251\",\"rbt\":\"2208867\",\"car\":\"1\",\"amp\":\"10\"")
        self.wfile.write(",\"err\":\"0\",\"ast\":\"0\",\"alw\":\"1\",\"stp\":\"0\",\"cbl\":\"0\",\"pha\":\"8\"")
        self.wfile.write(",\"tmp\":\"30\",\"dws\":\"0\",\"dwo\":\"0\",\"adi\":\"1\",\"uby\":\"0\",\"eto\":\"120\"")
        self.wfile.write(",\"wst\":\"3\",\"nrg\":[2,0,0,235,0,0,0,0,0,0,0,0,0,0,0,0],\"fwv\":\"020-rc1\"")
        self.wfile.write(",\"sse\":\"000000\",\"wss\":\"goe\",\"wke\":\"\",\"wen\":\"1\",\"tof\":\"101\"")
        self.wfile.write(",\"tds\":\"1\",\"lbr\":\"255\",\"aho\":\"2\",\"afi\":\"8\",\"ama\":\"32\",\"al1\":\"11\"")
        self.wfile.write(",\"al2\":\"12\",\"al3\":\"15\",\"al4\":\"24\",\"al5\":\"31\",\"cid\":\"255\"")
        self.wfile.write(",\"cch\":\"65535\",\"cfi\":\"65280\",\"lse\":\"0\",\"ust\":\"0\",\"wak\":\"\",\"r1x\":\"2\"")
        self.wfile.write(",\"dto\":\"0\",\"nmo\":\"0\",\"eca\":\"0\",\"ecr\":\"0\",\"ecd\":\"0\",\"ec4\":\"0\"")
        self.wfile.write(",\"ec5\":\"0\",\"ec6\":\"0\",\"ec7\":\"0\",\"ec8\":\"0\",\"ec9\":\"0\",\"ec1\":\"0\"")
        self.wfile.write(",\"rca\":\"\",\"rcr\":\"\",\"rcd\":\"\",\"rc4\":\"\",\"rc5\":\"\",\"rc6\":\"\"")
        self.wfile.write(",\"rc7\":\"\",\"rc8\":\"\",\"rc9\":\"\",\"rc1\":\"\",\"rna\":\"\",\"rnm\":\"\"")
        self.wfile.write(",\"rne\":\"\",\"rn4\":\"\",\"rn5\":\"\",\"rn6\":\"\",\"rn7\":\"\",\"rn8\":\"\"")
        self.wfile.write(",\"rn9\":\"\",\"rn1\":\"\"}")

    def do_GET(self):
        self._set_headers()
        if self.path == '/status':
            do_status(self)

    def do_HEAD(self):
        self._set_headers()
        
    def do_POST(self):
        # Doesn't do anything with posted data
        self._set_headers()
        self.wfile.write("<html><body><h1>POST!</h1></body></html>")
        
def run(server_class=HTTPServer, handler_class=S, port=8999):
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    print 'Starting httpd...'
    httpd.serve_forever()

if __name__ == "__main__":
    from sys import argv

    if len(argv) == 2:
        run(port=int(argv[1]))
    else:
        run()
