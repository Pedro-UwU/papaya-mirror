from http.server import BaseHTTPRequestHandler, HTTPServer
import json
import logging
from urllib.parse import urlparse, parse_qs

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(message)s")
logger = logging.getLogger("simple_api")
bar_entities = []


class SimpleAPI(BaseHTTPRequestHandler):
    def _set_headers(self, status_code=200, content_type="application/json"):
        self.send_response(status_code)
        self.send_header("Content-Type", content_type)
        self.end_headers()

    def log_request_info(self, body=None):
        parsed_path = urlparse(self.path)
        logger.info(
            f"\n##########################################\n"
            f"{self.command} request to {self.path}\n"
            f"\tHeaders: {self.headers}\n"
            f"\tPath: {parsed_path.path}\n"
            f"\tQuery Params: {parse_qs(parsed_path.query)}\n"
            f"\tBody: {body}\n"
            f"##########################################\n"
        )

    def do_GET(self):
        parsed_path = urlparse(self.path)
        self.log_request_info()

        if parsed_path.path == "/foo":
            self._set_headers()
            self.wfile.write(json.dumps({"message": "This is /foo"}).encode())
        elif parsed_path.path == "/bar":
            self._set_headers()
            self.wfile.write(json.dumps(bar_entities).encode())
        else:
            self._set_headers(404)
            self.wfile.write(json.dumps({"error": "Not Found"}).encode())

    def do_POST(self):
        parsed_path = urlparse(self.path)

        if parsed_path.path == "/bar":
            content_type = self.headers.get('Content-Type', '')

            if content_type.startswith('application/json'):
                # Handle JSON payload
                content_length = int(self.headers.get("Content-Length", 0))
                body = self.rfile.read(content_length).decode("utf-8")
                self.log_request_info(body=body)

                try:
                    entity = json.loads(body)
                except json.JSONDecodeError:
                    self._set_headers(400)
                    self.wfile.write(json.dumps({"error": "Invalid JSON"}).encode())
                    return

            elif content_type.startswith('application/x-www-form-urlencoded'):
                # Handle form data
                content_length = int(self.headers.get("Content-Length", 0))
                form_data = self.rfile.read(content_length).decode('utf-8')
                self.log_request_info(body=form_data)

                # Parse the form data
                form_params = {}
                for param in form_data.split('&'):
                    if '=' in param:
                        key, value = param.split('=', 1)
                        from urllib.parse import unquote_plus
                        form_params[key] = unquote_plus(value)

                entity = form_params

            else:
                self._set_headers(400)
                self.wfile.write(json.dumps({"error": "Unsupported media type"}).encode())
                return

            # Validate required fields
            for field in ["username", "password", "name", "email"]:
                if field not in entity:
                    self._set_headers(400)
                    self.wfile.write(
                        json.dumps(
                            {"error": f"Missing field: {field}"}).encode()
                    )
                    return

            # Check if username already exists
            if any(e["username"] == entity["username"] for e in bar_entities):
                self._set_headers(400)
                self.wfile.write(
                    json.dumps(
                        {"error": "Username already exists"}).encode()
                )
                return

            # Add new entity
            bar_entities.append(entity)
            self._set_headers(201)
            self.wfile.write(
                json.dumps(
                    {"message": "Entity created successfully", "entity": entity}).encode()
            )
        else:
            self._set_headers(404)
            self.wfile.write(json.dumps({"error": "Not Found"}).encode())


def run(server_class=HTTPServer, handler_class=SimpleAPI, port=8000):
    server_address = ("", port)
    httpd = server_class(server_address, handler_class)
    logger.info(f"Starting server on port {port}")
    httpd.serve_forever()


if __name__ == "__main__":
    run()
