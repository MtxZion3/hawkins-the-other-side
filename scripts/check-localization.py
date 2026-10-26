"""Validate shipped receiver copy and Android resource parity; no Android device needed."""
import json
from pathlib import Path
import re
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'app/src/main/res'
JAVA = ROOT / 'app/src/main/java/com/example/hawkins'


def strings(folder):
    return {node.attrib['name']: node.text or '' for node in ET.parse(RES / folder / 'strings.xml').getroot()}


class LocalizationContract(unittest.TestCase):
    def setUp(self):
        self.en = strings('values')
        self.zh = strings('values-zh')
        self.graph = json.loads((ROOT / 'app/src/main/assets/receiver-design.json').read_text())
        self.nodes = {node['id']: node for node in self.graph['nodes']}

    def resource(self, value):
        self.assertTrue(value.startswith('@string/'), value)
        self.assertIn(value.removeprefix('@string/'), self.en)

    def test_resource_parity_and_format_arguments(self):
        self.assertEqual(self.en.keys(), self.zh.keys())
        for key in self.en:
            self.assertTrue(self.en[key] and self.zh[key], key)
            self.assertEqual(re.findall(r'%\d+\$[.\d]*[dfs]', self.en[key]), re.findall(r'%\d+\$[.\d]*[dfs]', self.zh[key]), key)

    def test_all_layout_copy_and_accessibility_use_resources(self):
        for node in self.nodes.values():
            if 'accessibilityLabel' in node:
                self.resource(node['accessibilityLabel'])
            if node['kind'] == 'text' and not node.get('bindings', {}).get('content'):
                self.resource(node['content'])
            if node.get('component') == 'sui-button' and not node.get('bindings', {}).get('text'):
                self.resource(node['props']['text'])
            if node.get('component') == 'sui-segment-control':
                for option in node['props']['options']:
                    self.resource(option)

    def test_every_phase_loading_and_error_is_localized(self):
        props = self.nodes['control-panel']['props']
        phases = props['puzzle']['phaseCopy']
        self.assertEqual(set(phases), {'search', 'locked', 'listening', 'reply', 'wrong', 'solved'})
        for copy in phases.values():
            for value in copy:
                self.resource(value)
        for state in [props['loading'], self.graph['dataCases']['error']]:
            self.resource(state['status'])
            self.resource(state['signalCaption'])
        for value in props['puzzle']['labels'].values():
            self.resource(value)

    def test_resource_resolver_covers_layout_references(self):
        resolver = (JAVA / 'ui/room/components/ReceiverStrings.kt').read_text()
        pairs = re.findall(r'"@string/(\w+)" to R.string.(\w+)', resolver)
        self.assertEqual(dict(pairs), {key: key for key in self.en})

    def test_puzzle_symbols_are_not_translated(self):
        for letter in self.nodes['control-panel']['props']['puzzle']['keys']:
            self.assertEqual(self.en['letter_' + letter.lower()], letter)
            self.assertEqual(self.zh['letter_' + letter.lower()], letter)
        for action in self.graph['actions']:
            if action.get('payload', {}).get('operation') == 'reply-letter':
                self.assertIn(action['payload']['letter'], 'NARSUO')

    def test_manifest_declares_both_app_locales(self):
        android = '{http://schemas.android.com/apk/res/android}'
        locales = ET.parse(RES / 'xml/locales_config.xml').getroot()
        self.assertEqual({n.attrib[android + 'name'] for n in locales}, {'en', 'zh-CN'})
        app = ET.parse(ROOT / 'app/src/main/AndroidManifest.xml').getroot().find('application')
        self.assertEqual(app.attrib[android + 'localeConfig'], '@xml/locales_config')

    def test_no_inline_user_facing_kotlin_literals(self):
        for path in JAVA.rglob('*.kt'):
            source = path.read_text()
            self.assertIsNone(re.search(r'\bText\(\s*"|contentDescription\s*=\s*"', source), str(path))


if __name__ == '__main__':
    unittest.main()
