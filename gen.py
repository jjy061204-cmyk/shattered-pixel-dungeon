import os, sys, json, torch
from diffusers import StableDiffusionPipeline, DPMSolverMultistepScheduler
torch.set_num_threads(os.cpu_count())
name = sys.argv[1]
P = json.load(open('prompts.json'))[name]
model = P.get('model', 'gsdf/Counterfeit-V2.5')
pipe = StableDiffusionPipeline.from_pretrained(model, torch_dtype=torch.float32, safety_checker=None, requires_safety_checker=False)
pipe.scheduler = DPMSolverMultistepScheduler.from_config(pipe.scheduler.config, use_karras_sigmas=True)
NEG = ("nsfw, nude, naked, nipples, cleavage, lowres, bad anatomy, bad hands, text, error, missing fingers, extra digit, "
       "fewer digits, cropped, out of frame, worst quality, low quality, normal quality, jpeg artifacts, signature, watermark, "
       "username, blurry, multiple views, reference sheet, 2girls, 2boys, border, frame")
os.makedirs('out2', exist_ok=True)
from rembg import remove, new_session
sess = new_session('isnet-anime')
for seed in P['seeds']:
    g = torch.Generator().manual_seed(seed)
    im = pipe(P['prompt'], negative_prompt=NEG + ', ' + P.get('neg', ''), width=P.get('w', 512), height=P.get('h', 768),
              num_inference_steps=P.get('steps', 24), guidance_scale=7.5, generator=g).images[0]
    im.save(f'out2/{name}_{seed}.png')
    remove(im, session=sess).save(f'out2/{name}_{seed}_cut.png')
    print('saved', name, seed, flush=True)
